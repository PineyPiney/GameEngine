package com.pineypiney.game_engine.rendering.meshes.vulkan

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanManager
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK12
import java.nio.ByteBuffer

abstract class VulkanMesh(val vulkan: VulkanManager, override val attributes: Map<VertexAttribute<*, *>, Long>) : Mesh {

	override val stride by lazy { this.attributes.keys.sumOf { it.bytes } }
	abstract val count: Int

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	abstract val vertexBuffer: VmaBuffer
	abstract val vertexBufferAddress: Long

	override fun getData(): ByteBuffer {
		val size = vertexBuffer.info.size()

		MemoryStack.stackPush().use { stack ->
			// This staging buffer will be filled on the GPU and then read from the CPU
			val staging = VmaBuffer.create(vulkan.device, stack, size, VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT, Vma.VMA_MEMORY_USAGE_CPU_ONLY, "Indexed Mesh Download Staging")

			// Copy the data from vertex buffer into the staging buffer on the GPU
			vulkan.submitter.submitImmediate { cmd ->
				cmd.copyBuffer(vertexBuffer.buffer, staging.buffer, 0L, 0L, size)
			}

			// Read the staging buffer data on the CPU
			val pData = MemoryUtil.memAllocPointer(1).put(staging.info.pMappedData())
			val data = pData.getByteBuffer(0, size.toInt())
			staging.delete()
			return data
		}
	}

	override fun delete() {
		vertexBuffer.delete()
	}
}