package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.util.vma.VmaAllocatorCreateInfo
import org.lwjgl.util.vma.VmaVulkanFunctions
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDevice

class VulkanDevice(val device: VkDevice, val physicalDevice: VulkanPhysicalDevice, val queueFamilyIndex: Int) : Deletable {

	val allocator: Long

	init {
		val vulkanFunctions = VmaVulkanFunctions.calloc().set(device.physicalDevice.instance, device)

		val allocatorInfo = VmaAllocatorCreateInfo.calloc()
			.device(device)
			.physicalDevice(device.physicalDevice)
			.instance(device.physicalDevice.instance)
			.pVulkanFunctions(vulkanFunctions)
			.flags(Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT)

		val buffer = MemoryUtil.memAllocPointer(1)
		val err = Vma.vmaCreateAllocator(allocatorInfo, buffer)
		allocator = buffer[0]
		buffer.free()
		VkUtil.processError(err, "Failed to create Vulkan Memory Allocator")
	}

	fun waitIdle() = VK10.vkDeviceWaitIdle(device)

	override fun delete() {
		Vma.vmaDestroyAllocator(allocator)
		VK10.vkDestroyDevice(device, null)
	}
}