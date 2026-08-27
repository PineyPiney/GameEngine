package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.extension_functions.getVec2i
import com.pineypiney.game_engine.util.extension_functions.getVec3i
import com.pineypiney.game_engine.vulkan.VkUtil.processResult
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*

class VulkanPhysicalDevice(val physicalDevice: VkPhysicalDevice) {

	constructor(instance: VkInstance, handle: Long) : this(VkPhysicalDevice(handle, instance))

	val properties = getGpuProperties()
	val name = properties.properties().deviceNameString()
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

	fun getGpuMemoryFormatProperties(format: TextureFormat): VkFormatProperties2 {
		val properties = VkFormatProperties2.calloc().`sType$Default`()
		VK14.vkGetPhysicalDeviceFormatProperties2(physicalDevice, format.vulkan, properties)
		return properties
	}

	fun getImageFormatProperties(format: Int, type: Int, tiling: Int, usage: Int, flags: Int): VkImageFormatProperties2? {
		val formatInfo = VkPhysicalDeviceImageFormatInfo2.calloc()
			.`sType$Default`()
			.format(format)
			.type(type)
			.tiling(tiling)
			.usage(usage)
			.flags(flags)
		val formatProperties = VkImageFormatProperties2.calloc().`sType$Default`()
		val err = VK11.vkGetPhysicalDeviceImageFormatProperties2(physicalDevice, formatInfo, formatProperties)
		formatInfo.free()
		if (err == VK10.VK_ERROR_FORMAT_NOT_SUPPORTED) {
			formatProperties.free()
			return null
		}
		return formatProperties
	}

	fun getLimits() = properties.properties().limits()

	fun getMaxComputeWorkGroupSize() = getLimits().maxComputeWorkGroupSize().getVec3i()
	fun getMaxComputeWorkGroupCount() = getLimits().maxComputeWorkGroupCount().getVec3i()
	fun getMaxPushConstantsSize() = getLimits().maxPushConstantsSize()
	fun getMaxViewportDimensions() = getLimits().maxViewportDimensions().getVec2i()

	fun getSurfaceFormats(surface: VulkanSurface): VkSurfaceFormatKHR.Buffer {
		return VkUtil.getDeviceBuffer("Physical Device Surface Formats", physicalDevice, surface.handle, KHRSurface::vkGetPhysicalDeviceSurfaceFormatsKHR, VkSurfaceFormatKHR::calloc)
	}

	fun getSurfaceColour(surface: VulkanSurface): Pair<Int, Int> {
		val formats = getSurfaceFormats(surface)

		val colourFormat = if (formats.capacity() == 1 && formats[0].format() == VK10.VK_FORMAT_UNDEFINED) VK10.VK_FORMAT_B8G8R8A8_UNORM
		else formats[0].format()

		val colourSpace = formats[0].colorSpace()
		formats.free()

		return colourFormat to colourSpace
	}

	fun getQueueFamilies(stack: MemoryStack): Iterable<VulkanQueueFamily> {
		val properties = VkUtil.getStackBuffer(stack, physicalDevice, VK10::vkGetPhysicalDeviceQueueFamilyProperties, VkQueueFamilyProperties::calloc)
		return properties.mapIndexed { index, familyProperties -> VulkanQueueFamily(familyProperties, index, this) }
	}

	fun createDevice(): VulkanDevice {

		MemoryStack.stackPush().use { stack ->
			val properties = getQueueFamilies(stack)
			var index = properties.indexOfFirst { property ->
				property.supportsGraphics()
			}
			if (index == -1) index = properties.count()

			val priorities = stack.mallocFloat(1).put(0f)
			priorities.position(0)

			val queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1, stack)
				.`sType$Default`()
				.queueFamilyIndex(index)
				.pQueuePriorities(priorities)

			val swapchainExt = stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
			val extensions = stack.mallocPointer(1).put(swapchainExt).flip()

			val deviceFeatures = features.features().geometryShader(true)

			val deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
				.`sType$Default`()
				.pNext(VkPhysicalDeviceBufferDeviceAddressFeatures.calloc(stack).`sType$Default`().bufferDeviceAddress(true))
				.pNext(VkPhysicalDeviceSynchronization2Features.calloc(stack).`sType$Default`().synchronization2(true))
				.pNext(VkPhysicalDeviceDynamicRenderingFeatures.calloc(stack).`sType$Default`().dynamicRendering(true))
				.pQueueCreateInfos(queueCreateInfo)
				.ppEnabledExtensionNames(extensions)
				.pEnabledFeatures(deviceFeatures)

			val pointer = stack.mallocPointer(1)
			processResult(VK10.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pointer), "Failed to create Vulkan device")

			return VulkanDevice(VkDevice(pointer.get(), physicalDevice, deviceCreateInfo), this, index)
		}
	}
}