package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.getVec2i
import com.pineypiney.game_engine.util.extension_functions.getVec3i
import com.pineypiney.game_engine.vulkan.VkUtil.processError
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

class VulkanPhysicalDevice(val physicalDevice: VkPhysicalDevice) {

	constructor(instance: VkInstance, handle: Long) : this(VkPhysicalDevice(handle, instance))

	val properties = getGpuProperties()
	val memoryProperties = getGpuMemoryProperties()
	val features = VkPhysicalDeviceFeatures2.calloc().`sType$Default`()

	val supportsBDA: Boolean

	init {
		val bdaFeatures = VkPhysicalDeviceBufferDeviceAddressFeatures.calloc().`sType$Default`()
		features.pNext(bdaFeatures)
		VK13.vkGetPhysicalDeviceFeatures2(physicalDevice, features)
		supportsBDA = bdaFeatures.bufferDeviceAddress()
		features.features().geometryShader(true)
	}

	@Throws(RuntimeException::class)
	fun getMemoryType(typeFilter: Int, propertyFlags: Int): Int {
		for ((i, type) in memoryProperties.memoryTypes().withIndex()) {
			if (typeFilter and (1 shl i) != 0
				&& (type.propertyFlags() and propertyFlags == propertyFlags)
			) return i
		}
		throw RuntimeException("Failed to find memory type")
	}

	fun getGpuProperties(): VkPhysicalDeviceProperties2 {
		val properties = VkPhysicalDeviceProperties2.calloc().`sType$Default`()
		VK13.vkGetPhysicalDeviceProperties2(physicalDevice, properties)
		return properties
	}

	fun getGpuMemoryProperties(): VkPhysicalDeviceMemoryProperties {
		val memoryProperties = VkPhysicalDeviceMemoryProperties.calloc()
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties)
		return memoryProperties
	}

	fun getLimits() = properties.properties().limits()

	fun getMaxComputeWorkGroupSize() = getLimits().maxComputeWorkGroupSize().getVec3i()
	fun getMaxComputeWorkGroupCount() = getLimits().maxComputeWorkGroupCount().getVec3i()
	fun getMaxPushConstantsSize() = getLimits().maxPushConstantsSize()
	fun getMaxViewportDimensions() = getLimits().maxViewportDimensions().getVec2i()

	fun getSurfaceFormats(surface: VulkanSurface): VkSurfaceFormatKHR.Buffer {
		return VkUtil.getBuffer("Physical Device Surface Formats", physicalDevice, surface.handle, KHRSurface::vkGetPhysicalDeviceSurfaceFormatsKHR, VkSurfaceFormatKHR::calloc)
	}

	fun getQueueFamilies(): Iterable<VulkanQueueFamily> {
		val properties = VkUtil.getBuffer(physicalDevice, VK10::vkGetPhysicalDeviceQueueFamilyProperties, VkQueueFamilyProperties::calloc)
		return properties.mapIndexed { index, familyProperties -> VulkanQueueFamily(familyProperties, index, this) }
	}

	fun createDevice(deletionQueue: DeletionQueue): VulkanDevice {

		val properties = getQueueFamilies()
		var index = properties.indexOfFirst { property ->
			property.supportsGraphics()
		}
		if (index == -1) index = properties.count()
		properties.delete()

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

		val deviceFeatures = features.features()
			.geometryShader(true)

		val deviceCreateInfo = VkDeviceCreateInfo.calloc()
			.`sType$Default`()
			.pNext(VkPhysicalDeviceBufferDeviceAddressFeatures.calloc().`sType$Default`().bufferDeviceAddress(true))
			.pNext(VkPhysicalDeviceSynchronization2Features.calloc().`sType$Default`().synchronization2(true))
			.pNext(VkPhysicalDeviceDynamicRenderingFeatures.calloc().`sType$Default`().dynamicRendering(true))
			.pQueueCreateInfos(queueCreateInfo)
			.ppEnabledExtensionNames(extensions)
			.pEnabledFeatures(deviceFeatures)

		val pointer = MemoryUtil.memAllocPointer(1)
		processError(VK10.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pointer), "Failed to create Vulkan device") {
			pointer.free()
		}

		val device = VulkanDevice(VkDevice(pointer.get(), physicalDevice, deviceCreateInfo), this, index, deletionQueue)
		pointer.free()
		deviceCreateInfo.free()
		swapchainExt.free()
		extensions.free()
		priorities.free()
		return device
	}
}