package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.vulkan.VkUtil.getBuffer
import com.pineypiney.game_engine.vulkan.VkUtil.processError
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

class VulkanPhysicalDevice(val physicalDevice: VkPhysicalDevice) {

	constructor(instance: VkInstance, handle: Long) : this(VkPhysicalDevice(handle, instance))

	val properties = getMemoryProperties()
	val features = VkPhysicalDeviceFeatures2.calloc().`sType$Default`()

	val supportsBDA: Boolean

	init {
		val bdaFeatures = VkPhysicalDeviceBufferDeviceAddressFeatures.calloc().`sType$Default`()
		features.pNext(bdaFeatures)
		VK13.vkGetPhysicalDeviceFeatures2(physicalDevice, features)
		supportsBDA = bdaFeatures.bufferDeviceAddress()
	}

	@Throws(RuntimeException::class)
	fun getMemoryType(typeFilter: Int, propertyFlags: Int): Int {
		for ((i, type) in properties.memoryTypes().withIndex()) {
			if (typeFilter and (1 shl i) != 0
				&& (type.propertyFlags() and propertyFlags == propertyFlags)
			) return i
		}
		throw RuntimeException("Failed to find memory type")
	}

	fun getMemoryProperties(): VkPhysicalDeviceMemoryProperties {
		val properties = VkPhysicalDeviceMemoryProperties.calloc()
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, properties)
		return properties
	}

	fun createDevice(): VulkanDevice {

		val properties = getBuffer(physicalDevice, VK10::vkGetPhysicalDeviceQueueFamilyProperties, VkQueueFamilyProperties::calloc)
		var index = properties.indexOfFirst { property ->
			property.queueFlags() and VK10.VK_QUEUE_GRAPHICS_BIT != 0
		}
		if (index == -1) index = properties.capacity()
		properties.free()

		val priorities = MemoryUtil.memAllocFloat(1).put(0f)
		priorities.position(0)

		val queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
			.`sType$Default`()
			.queueFamilyIndex(index)
			.pQueuePriorities(priorities)

		val extensions = MemoryUtil.memAllocPointer(1)
		val swapchainExt = MemoryUtil.memUTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
		extensions.put(swapchainExt)
		extensions.flip()

		val deviceCreateInfo = VkDeviceCreateInfo.calloc()
			.`sType$Default`()
			.pNext(VkPhysicalDeviceBufferDeviceAddressFeatures.calloc().`sType$Default`().bufferDeviceAddress(true))
			.pNext(VkPhysicalDeviceSynchronization2Features.calloc().`sType$Default`().synchronization2(true))
			.pQueueCreateInfos(queueCreateInfo)
			.ppEnabledExtensionNames(extensions)

		val pointer = MemoryUtil.memAllocPointer(1)
		processError(VK10.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pointer), "Failed to create Vulkan device") {
			pointer.free()
		}

		val device = VulkanDevice(VkDevice(pointer.get(), physicalDevice, deviceCreateInfo), this, index)
		pointer.free()
		deviceCreateInfo.free()
		swapchainExt.free()
		extensions.free()
		priorities.free()
		return device
	}
}