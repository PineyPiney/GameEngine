package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.util.vma.VmaAllocationCreateInfo
import org.lwjgl.util.vma.VmaAllocationInfo
import org.lwjgl.vulkan.VkBufferCreateInfo

class VmaBuffer(val device: VulkanDevice, val buffer: Long, val allocation: Long, val info: VmaAllocationInfo) : Deletable {

	fun getBuffer(size: Int) = MemoryUtil.memByteBuffer(info.pMappedData(), size)
	fun getBuffer(offset: Long, size: Int) = MemoryUtil.memByteBuffer(info.pMappedData() + offset, size)

	fun unmap() {
		Vma.vmaUnmapMemory(device.allocator, allocation)
	}

	override fun delete() {
		Vma.vmaDestroyBuffer(device.allocator, buffer, allocation)
	}

	companion object {
		fun create(device: VulkanDevice, stack: MemoryStack, size: Long, usage: Int, allocationUsage: Int): VmaBuffer {

			val bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
				.`sType$Default`()
				.size(size)
				.usage(usage)
			val allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
				.usage(allocationUsage)
				.flags(Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT)

			val pBuffer = stack.mallocLong(1)
			val pAllocation = stack.mallocPointer(1)
			val info = VmaAllocationInfo.calloc()

			VkUtil.processError(Vma.vmaCreateBuffer(device.allocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, info), "Failed to create VMA Buffer")
			return VmaBuffer(device, pBuffer[0], pAllocation[0], info)
		}

		fun create(device: VulkanDevice, size: Long, usage: Int, allocationUsage: Int): VmaBuffer {
			MemoryStack.stackPush().use { stack ->
				return create(device, stack, size, usage, allocationUsage)
			}
		}
	}
}