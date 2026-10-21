package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.LwjglUtil
import com.pineypiney.game_engine.util.extension_functions.getVec2i
import com.pineypiney.game_engine.util.extension_functions.getVec3i
import com.pineypiney.game_engine.util.extension_functions.stringsToPointer
import com.pineypiney.game_engine.vulkan.VkUtil.processResult
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Struct
import org.lwjgl.vulkan.*

class VulkanPhysicalDevice(val physicalDevice: VkPhysicalDevice) {

	constructor(instance: VkInstance, handle: Long) : this(VkPhysicalDevice(handle, instance))

	val properties = getGpuProperties()
	val name = properties.deviceNameString()
	val memoryProperties = getGpuMemoryProperties()

	val features = VkPhysicalDeviceFeatures2.calloc().`sType$Default`()
	val extraFeatures = mutableListOf<Feature<*>>()

	init {
		VK13.vkGetPhysicalDeviceFeatures2(physicalDevice, features)
		getLimits()

		for(format in TextureFormat.entries) {
			format.supported = getImageFormatProperties(format.vulkan, VK10.VK_IMAGE_TYPE_2D, VK10.VK_IMAGE_TILING_OPTIMAL, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, 0) != null
		}
		GameEngineI.logger.info("GPU $name supports to following image formats: ${TextureFormat.entries.filter(TextureFormat::supported).joinToString()}")
	}

	fun <S: Struct<S>> addFeature(struct: S, add: (VkDeviceCreateInfo, S) -> VkDeviceCreateInfo): VulkanPhysicalDevice{
		extraFeatures.add(Feature(struct, add))
		return this
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

	fun getGpuProperties(): VkPhysicalDeviceProperties {
		val properties = VkPhysicalDeviceProperties.calloc()
		VK10.vkGetPhysicalDeviceProperties(physicalDevice, properties)
		return properties
	}

	fun getGpuMemoryProperties(): VkPhysicalDeviceMemoryProperties {
		val memoryProperties = VkPhysicalDeviceMemoryProperties.calloc()
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties)
		return memoryProperties
	}

	fun getGpuMemoryFormatProperties(format: TextureFormat): VkFormatProperties2 {
		val properties = VkFormatProperties2.calloc().`sType$Default`()
		VK11.vkGetPhysicalDeviceFormatProperties2(physicalDevice, format.vulkan, properties)
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

	fun getLimits() = properties.limits()

	fun getMaxTextureSide1D() = getLimits().maxImageDimension1D()
	fun getMaxTextureSide2D() = getLimits().maxImageDimension2D()
	fun getMaxTextureSide3D() = getLimits().maxImageDimension3D()
	fun getVertexAttribs() = getLimits().maxVertexInputAttributes()
	fun getMaxViewportDimensions() = getLimits().maxViewportDimensions().getVec2i()
	fun getMaxTesselationPatchVertices(): Int = getLimits().maxTessellationPatchSize()
	fun getMaxTesselationLevel(): Int = getLimits().maxTessellationGenerationLevel()
	fun getMaxComputeWorkGroupSize() = getLimits().maxComputeWorkGroupSize().getVec3i()
	fun getMaxComputeWorkGroupCount() = getLimits().maxComputeWorkGroupCount().getVec3i()
	fun getMaxPushConstantsSize() = getLimits().maxPushConstantsSize()

	fun getSurfaceFormats(stack: MemoryStack, surface: VulkanSurface): VkSurfaceFormatKHR.Buffer {
		return LwjglUtil.enumerateStructs(stack, "Physical Device Surface Formats", physicalDevice, surface.handle, VkSurfaceFormatKHR::calloc, KHRSurface::vkGetPhysicalDeviceSurfaceFormatsKHR) ?: VkSurfaceFormatKHR.calloc(0, stack)
	}

	fun getSurfaceColour(surface: VulkanSurface): Pair<Int, Int> {
		MemoryStack.stackPush().use { stack ->
			val formats = getSurfaceFormats(stack, surface)

			val colourFormat = if (formats.capacity() == 1 && formats[0].format() == VK10.VK_FORMAT_UNDEFINED) VK10.VK_FORMAT_B8G8R8A8_UNORM
			else formats[0].format()

			val colourSpace = formats[0].colorSpace()

			return colourFormat to colourSpace
		}
	}

	fun getQueueFamilies(stack: MemoryStack): Iterable<VulkanQueueFamily> {
		val properties = LwjglUtil.enumerateStructs(stack, physicalDevice, VkQueueFamilyProperties::calloc, VK10::vkGetPhysicalDeviceQueueFamilyProperties)
		return properties?.mapIndexed { index, familyProperties -> VulkanQueueFamily(familyProperties, index, this) } ?: emptyList()
	}

	fun createDevice(extensions: List<String>): VulkanDevice {

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

			val availableExtensions = extensions.intersect(VkUtil.getAvailableDeviceExtensions(physicalDevice).toSet())
			val extensionsPtr: PointerBuffer = stack.stringsToPointer(availableExtensions)

			val deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
				.`sType$Default`()
				.pQueueCreateInfos(queueCreateInfo)
				.ppEnabledExtensionNames(extensionsPtr)
				.pEnabledFeatures(features.features())

			for(feature in extraFeatures){
				feature.add(deviceCreateInfo)
			}

			val pointer = stack.mallocPointer(1)
			processResult(VK10.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pointer), "Failed to create Vulkan device")

			return VulkanDevice(VkDevice(pointer.get(), physicalDevice, deviceCreateInfo), this, index)
		}
	}

	fun free(){
		features.free()
		for(feature in extraFeatures) feature.struct.free()
		memoryProperties.free()
		properties.free()
	}

	class Feature<S: Struct<S>>(val struct: S, val add: (VkDeviceCreateInfo, S) -> VkDeviceCreateInfo){
		fun add(info: VkDeviceCreateInfo) = add(info, struct)
	}
}