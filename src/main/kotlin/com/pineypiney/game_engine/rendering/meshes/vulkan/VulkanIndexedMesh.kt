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

open class VulkanIndexedMesh(vulkan: VulkanManager, name: String, verticesData: ByteBuffer, indicesData: ByteBuffer, attributes: Map<VertexAttribute<*, *>, Long>) : VulkanMesh(vulkan, attributes) {

	override val stride by lazy { this.attributes.keys.sumOf { it.bytes } }
	override val count = indicesData.capacity() / 4

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	final override val vertexBuffer: VmaBuffer
	val indexBuffer: VmaBuffer
	final override val vertexBufferAddress: Long

	init {

		MemoryStack.stackPush().use { stack ->
			vertexBuffer = VmaBuffer.create(
				vulkan.device, stack, verticesData.capacity().toLong(),
				VK12.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT or
						VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT or
						VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT or
						VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
				Vma.VMA_MEMORY_USAGE_GPU_ONLY, "$name Mesh Vertices"
			)
			indexBuffer = VmaBuffer.create(
				vulkan.device, stack, indicesData.capacity().toLong(),
				VK12.VK_BUFFER_USAGE_INDEX_BUFFER_BIT or VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
				Vma.VMA_MEMORY_USAGE_GPU_ONLY, "$name Mesh Indices"
			)
			vertexBufferAddress = vulkan.device.getBufferAddress(stack, vertexBuffer)


			// This staging buffer will be filled on the CPU and used to fill the GPU only buffers
			val staging = VmaBuffer.create(
				vulkan.device,
				stack,
				(verticesData.capacity() + indicesData.capacity()).toLong(),
				VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
				Vma.VMA_MEMORY_USAGE_CPU_ONLY,
				"Indexed Mesh Upload Staging"
			)

			// Fill the staging buffer with the data on the CPU
			val b = staging.getBuffer(verticesData.capacity() + indicesData.capacity())
			b.put(verticesData).put(indicesData)

			// Copy the data from staging into the vertex and index buffers on the GPU
			vulkan.submitter.submitImmediate { cmd ->
				cmd.copyBuffer(staging.buffer, vertexBuffer.buffer, 0L, 0L, verticesData.capacity().toLong())
				cmd.copyBuffer(staging.buffer, indexBuffer.buffer, verticesData.capacity().toLong(), 0L, indicesData.capacity().toLong())
			}

			staging.delete()
		}
	}

	override fun draw(api: RenderingApi, mode: Int) {
		api.bindIndices(indexBuffer.buffer, 0L, VK10.VK_INDEX_TYPE_UINT32)
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