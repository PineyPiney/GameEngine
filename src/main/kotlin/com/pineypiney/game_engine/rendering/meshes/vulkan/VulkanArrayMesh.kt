package com.pineypiney.game_engine.rendering.meshes.vulkan

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanManager
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK12
import java.nio.ByteBuffer

open class VulkanArrayMesh(vulkan: VulkanManager, name: String, verticesData: ByteBuffer, attributes: Map<VertexAttribute<*, *>, Long>) : VulkanMesh(vulkan, attributes) {

	override val stride by lazy { this.attributes.keys.sumOf { it.bytes } }
	override val count = verticesData.capacity() / stride

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	final override val vertexBuffer: VmaBuffer
	final override val vertexBufferAddress: Long

	init {

		MemoryStack.stackPush().use { stack ->
			vertexBuffer = VmaBuffer.create(
				vulkan.device, stack, verticesData.capacity().toLong(),
				VK12.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT or
						VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT or
						VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT or
						VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
				Vma.VMA_MEMORY_USAGE_GPU_ONLY, "$name Vertices"
			)
			vertexBufferAddress = vulkan.device.getBufferAddress(stack, vertexBuffer)


			// This staging buffer will be filled on the CPU and used to fill the GPU only buffers
			val staging = VmaBuffer.create(
				vulkan.device,
				stack,
				verticesData.capacity().toLong(),
				VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
				Vma.VMA_MEMORY_USAGE_CPU_ONLY,
				"Array Mesh Upload Staging"
			)

			// Fill the staging buffer with the data on the CPU
			staging.getBuffer().put(verticesData)

			// Copy the data from staging into the vertex and index buffers on the GPU
			vulkan.submitter.submitImmediate { cmd ->
				cmd.copyBuffer(staging.buffer, vertexBuffer.buffer, 0L, 0L, verticesData.capacity().toLong())
			}

			staging.delete()
		}
	}

	override fun draw(api: RenderingApi, mode: Int) {
		api.draw(count, 0, 0)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		api.drawInstanced(count, 0, amount, 0, 0)
	}
}