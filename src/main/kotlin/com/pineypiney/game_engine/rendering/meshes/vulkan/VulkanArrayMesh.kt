package com.pineypiney.game_engine.rendering.meshes.vulkan

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanManager
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK12
import java.nio.ByteBuffer

open class VulkanArrayMesh(vulkan: VulkanManager, name: String, buffers: List<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>) : VulkanMesh(vulkan) {

	final override val count: Int
	final override val buffers: List<VulkanVertexBuffer>

	init {

		MemoryStack.stackPush().use { stack ->
			this.buffers = buffers.map { (data, attribs) ->
				val vma = createBuffer(stack, vulkan.device, "$name Array Mesh", data.capacity().toLong())
				(VulkanVertexBuffer(vulkan, attribs.keys.sumOf { it.bytes }, attribs, data.capacity().toLong(), vma))
			}
			this.count = this.buffers.first().run { size / stride }.toInt()

			// This staging buffer will be filled on the CPU and used to fill the GPU only buffers
			val staging = VmaBuffer.create(
				vulkan.device,
				stack,
				buffers.sumOf { it.first.capacity() }.toLong(),
				VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
				Vma.VMA_MEMORY_USAGE_CPU_ONLY,
				"Array Mesh Upload Staging"
			)

			// Fill the staging buffer with the data on the CPU
			val stagingData = staging.getBuffer()
			for ((data, _) in buffers) stagingData.put(data)

			// Copy the data from staging into the vertex and index buffers on the GPU
			vulkan.submitter.submitImmediate { cmd ->
				var offset = 0L
				for (buffer in this.buffers) {
					cmd.copyBuffer(staging.buffer, buffer.vertexBuffer.buffer, offset, 0L, buffer.size)
					offset += buffer.size
				}
			}

			staging.delete()
		}
	}

	override fun bind(api: RenderingApi) {
		api.bindVertices(this)
	}

	override fun draw(api: RenderingApi, mode: Int) {
		api.draw(count, 0, 0)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		api.drawInstanced(count, 0, amount, 0, 0)
	}
}