package com.pineypiney.game_engine.rendering.meshes.vulkan

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import com.pineypiney.game_engine.vulkan.VulkanManager
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK12

abstract class VulkanMesh(val vulkan: VulkanManager) : Mesh {

	abstract override val buffers: List<VulkanVertexBuffer>
	abstract val count: Int

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	val vertexBuffer: VmaBuffer get() = buffers[0].vertexBuffer
//	val vertexBufferAddress: Long get() = buffers[0].vertexBufferAddress

	override fun delete() {
		buffers.delete()
	}

	companion object {
		fun createBuffer(stack: MemoryStack, device: VulkanDevice, name: String, size: Long): VmaBuffer {
			return VmaBuffer.create(
				device, stack, size,
				VK12.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT or
						VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT or
						VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT or
						VK12.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT or
						VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
				Vma.VMA_MEMORY_USAGE_GPU_ONLY, "$name Vertices"
			)
		}
	}
}