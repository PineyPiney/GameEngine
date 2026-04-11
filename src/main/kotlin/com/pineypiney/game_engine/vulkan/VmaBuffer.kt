package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.util.vma.VmaAllocationCreateInfo
import org.lwjgl.util.vma.VmaAllocationInfo
import org.lwjgl.vulkan.VkBufferCreateInfo

class VmaBuffer(val device: VulkanDevice, val buffer: Long, val allocation: Long, val info: VmaAllocationInfo) : Deletable {

	override fun delete() {
		Vma.vmaDestroyBuffer(device.allocator, buffer, allocation)
	}

	companion object {
		fun create(device: VulkanDevice, size: Long, usage: Int, allocationUsage: Int): VmaBuffer {

			val bufferCreateInfo = VkBufferCreateInfo.calloc()
				.`sType$Default`()
				.size(size)
				.usage(usage)
			val allocationCreateInfo = VmaAllocationCreateInfo.calloc()
				.usage(allocationUsage)
				.flags(Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT)

			val pBuffer = MemoryUtil.memAllocLong(1)
			val pAllocation = MemoryUtil.memAllocPointer(1)
			val info = VmaAllocationInfo.calloc()
			val err = Vma.vmaCreateBuffer(device.allocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, info)
			val buffer = VmaBuffer(device, pBuffer[0], pAllocation[0], info)

			bufferCreateInfo.free()
			allocationCreateInfo.free()
			pBuffer.free()
			pAllocation.free()

			VkUtil.processError(err, "Failed to create VMA Buffer")
			return buffer
		}
	}
}