package com.pineypiney.game_engine.rendering.meshes.vulkan

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanManager
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12
import java.nio.ByteBuffer

open class VulkanIndexedMesh(vulkan: VulkanManager, name: String, buffers: List<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>, indicesData: ByteBuffer) : VulkanMesh(vulkan) {

	override val count = indicesData.capacity() / 4

	final override val buffers: List<VulkanVertexBuffer>

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	val indexBuffer: VmaBuffer

	init {

		MemoryStack.stackPush().use { stack ->
			this.buffers = buffers.map { (data, attribs) ->
				val vma = createBuffer(stack, vulkan.device, "$name Array Mesh", data.capacity().toLong())
				(VulkanVertexBuffer(vulkan, attribs.keys.sumOf { it.bytes }, attribs, data.capacity().toLong(), vma))
			}

			indexBuffer = VmaBuffer.create(
				vulkan.device, stack, indicesData.capacity().toLong(),
				VK12.VK_BUFFER_USAGE_INDEX_BUFFER_BIT or VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
				Vma.VMA_MEMORY_USAGE_GPU_ONLY, "$name Mesh Indices"
			)

			// This staging buffer will be filled on the CPU and used to fill the GPU only buffers
			val staging = VmaBuffer.create(
				vulkan.device,
				stack,
				(buffers.sumOf { it.first.capacity() } + indicesData.capacity()).toLong(),
				VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
				Vma.VMA_MEMORY_USAGE_CPU_ONLY,
				"Indexed Mesh Upload Staging"
			)

			// Fill the staging buffer with the data on the CPU
			val b = staging.getBuffer()
			for ((data, _) in buffers) b.put(data)
			b.put(indicesData)

			// Copy the data from staging into the vertex and index buffers on the GPU
			vulkan.submitter.submitImmediate { cmd ->
				var offset = 0L
				for (buffer in this.buffers) {
					cmd.copyBuffer(staging.buffer, buffer.vertexBuffer.buffer, offset, 0L, buffer.size)
					offset += buffer.size
				}
				cmd.copyBuffer(staging.buffer, indexBuffer.buffer, offset, 0L, indicesData.capacity().toLong())
			}

			staging.delete()
		}
	}

	override fun bind(api: RenderingApi) {
		api.bindVertices(this)
		api.bindIndices(indexBuffer.buffer, 0L, VK10.VK_INDEX_TYPE_UINT32)
	}

	override fun draw(api: RenderingApi, mode: Int) {
		api.drawIndexed(count, 0, 0)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		api.drawIndexedInstanced(count, 0, amount, 0, 0)
	}

	override fun delete() {
		super.delete()
		indexBuffer.delete()
	}
}