package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.libc.LibCString
import org.lwjgl.util.vma.Vma
import org.lwjgl.util.vma.VmaAllocationCreateInfo
import org.lwjgl.util.vma.VmaAllocationInfo
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkBufferCreateInfo
import java.nio.ByteBuffer

class VmaBuffer(val device: VulkanDevice, val buffer: Long, val allocation: Long, val info: VmaAllocationInfo) : Deletable {

	fun setBuffer(data: ByteBuffer, offset: Long = 0L) = getBuffer(offset, data.remaining()).put(data)
	fun getBuffer(size: Int = info.size().toInt()): ByteBuffer = MemoryUtil.memByteBuffer(info.pMappedData(), size)
	fun getBuffer(offset: Long, size: Int): ByteBuffer = MemoryUtil.memByteBuffer(info.pMappedData() + offset, size)

	fun clear() {
		LibCString.memset(getBuffer(info.size().toInt()), 0)
	}

	fun unmap() {
		Vma.vmaUnmapMemory(device.allocator, allocation)
	}

	override fun delete() {
		Vma.vmaDestroyBuffer(device.allocator, buffer, allocation)
	}

	companion object {
		fun create(device: VulkanDevice, stack: MemoryStack, size: Long, usage: Int, allocationUsage: Int, name: String): VmaBuffer {

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

			VkUtil.processResult(Vma.vmaCreateBuffer(device.allocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, info), "Failed to create VMA Buffer")
			device.nameObject(pBuffer[0], VK10.VK_OBJECT_TYPE_BUFFER, name)
			return VmaBuffer(device, pBuffer[0], pAllocation[0], info)
		}

		fun create(device: VulkanDevice, size: Long, usage: Int, allocationUsage: Int, name: String): VmaBuffer {
			return MemoryStack.stackPush().use { stack ->
				create(device, stack, size, usage, allocationUsage, name)
			}
		}
	}
}