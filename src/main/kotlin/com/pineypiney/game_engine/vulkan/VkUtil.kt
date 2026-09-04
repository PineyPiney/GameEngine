package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorLayout
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipelineLayout
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPushConstantManager
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage3D
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import kool.map
import kool.mapIndexed
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.CustomBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Struct
import org.lwjgl.util.vma.Vma
import org.lwjgl.util.vma.VmaAllocationCreateInfo
import org.lwjgl.vulkan.*
import java.nio.IntBuffer
import java.nio.LongBuffer

object VkUtil {

	fun translateVulkanResult(result: Int): String {
		return when (result) {
			VK10.VK_SUCCESS -> "Command successfully completed."

			// Warnings are > 0
			VK10.VK_NOT_READY -> "A fence or query has not yet completed."
			VK10.VK_TIMEOUT -> "A wait operation has not completed in the specified time."
			VK10.VK_EVENT_SET -> "An event is signaled."
			VK10.VK_EVENT_RESET -> "An event is unsignaled."
			VK10.VK_INCOMPLETE -> "A return array was too small for the result."
			KHRSwapchain.VK_SUBOPTIMAL_KHR -> "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully."

			// Errors are < 0
			VK10.VK_ERROR_OUT_OF_HOST_MEMORY -> "A host memory allocation has failed."
			VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY -> "A device memory allocation has failed."
			VK10.VK_ERROR_INITIALIZATION_FAILED -> "Initialization of an object could not be completed for implementation-specific reasons."
			VK10.VK_ERROR_DEVICE_LOST -> "The logical or physical device has been lost."
			VK10.VK_ERROR_MEMORY_MAP_FAILED -> "Mapping of a memory object has failed."
			VK10.VK_ERROR_LAYER_NOT_PRESENT -> "A requested layer is not present or could not be loaded."
			VK10.VK_ERROR_EXTENSION_NOT_PRESENT -> "A requested extension is not supported."
			VK10.VK_ERROR_FEATURE_NOT_PRESENT -> "A requested feature is not supported."
			VK10.VK_ERROR_INCOMPATIBLE_DRIVER -> "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons."
			VK10.VK_ERROR_TOO_MANY_OBJECTS -> "Too many objects of the type have already been created."
			VK10.VK_ERROR_FORMAT_NOT_SUPPORTED -> "A requested format is not supported on this device."
			VK11.VK_ERROR_OUT_OF_POOL_MEMORY -> ""
			KHRSurface.VK_ERROR_SURFACE_LOST_KHR -> "A surface is no longer available."
			KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR -> "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API."
			KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR -> ("A surface has changed in such a way that it is no longer compatible with the swapchain, and further presentation requests using the "
					+ "swapchain will fail. Applications must query the new surface properties and recreate their swapchain if they wish to continue"
					+ "presenting to the surface.")

			KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR -> ("The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an"
					+ " image.")

			EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT -> "A validation layer found an error."
			else -> String.format("%s [%d]", "Unknown", result)
		}
	}

	fun isFail(res: Int) = res != VK10.VK_SUCCESS

	@Throws(AssertionError::class)
	fun processResult(res: Int, message: String) {
		if (res > VK10.VK_SUCCESS) {
			GameEngineI.logger.warn("$message: ${translateVulkanResult(res)}")
		} else if (res < VK10.VK_SUCCESS) {
			throw AssertionError("$message: ${translateVulkanResult(res)}")
		}
	}

	@Throws(AssertionError::class)
	fun <S : Struct<S>> getLong(name: String, device: VulkanDevice, stack: MemoryStack, struct: S, func: (VkDevice, S, VkAllocationCallbacks?, LongBuffer) -> Int): Long {
		val longBuffer = stack.mallocLong(1)
		processResult(func(device.device, struct, null, longBuffer), "Failed to create $name")
		return longBuffer[0]
	}

	@Throws(AssertionError::class)
	fun <S : Struct<S>> allocatePointer(name: String, device: VulkanDevice, stack: MemoryStack, struct: S, func: (VkDevice, S, PointerBuffer) -> Int): Long {
		val pointer = stack.mallocPointer(1)
		processResult(func(device.device, struct, pointer), "Failed to allocate $name")
		return pointer[0]
	}

	@Throws(AssertionError::class)
	fun <E, B : CustomBuffer<B>> getBuffer(name: String, instance: E, func: (E, IntBuffer, B?) -> Int, creator: (Int) -> B): B {
		MemoryStack.stackPush().use { stack ->
			return getStackBuffer(name, stack, instance, func, creator)
		}
	}

	@Throws(AssertionError::class)
	fun <E, B : CustomBuffer<B>> getStackBuffer(name: String, stack: MemoryStack, instance: E, func: (E, IntBuffer, B?) -> Int, creator: (Int) -> B): B {
		val count = stack.mallocInt(1)
		processResult(func(instance, count, null), "Failed to get number of $name")
		val buffer = creator(count[0])
		processResult(func(instance, count, buffer), "Failed to get $name")
		return buffer
	}

	@Throws(AssertionError::class)
	fun <E, B : CustomBuffer<B>> getBuffer(instance: E, func: (E, IntBuffer, B?) -> Unit, creator: (Int) -> B): B {
		MemoryStack.stackPush().use { stack ->
			return getStackBuffer(stack, instance, func, creator)
		}
	}

	@Throws(AssertionError::class)
	fun <E, B : CustomBuffer<B>> getStackBuffer(stack: MemoryStack, instance: E, func: (E, IntBuffer, B?) -> Unit, creator: (Int) -> B): B {
		val count = stack.mallocInt(1)
		func(instance, count, null)
		val buffer = creator(count[0])
		func(instance, count, buffer)
		return buffer
	}


	@Throws(AssertionError::class)
	fun <D, E, B> getDeviceBuffer(name: String, device: D, instance: E, func: (D, E, IntBuffer, B?) -> Int, creator: (Int) -> B): B {
		MemoryStack.stackPush().use { stack ->
			return getStackDeviceBuffer(name, device, stack, instance, func, creator)
		}
	}

	@Throws(AssertionError::class)
	fun <D, E, B> getStackDeviceBuffer(name: String, device: D, stack: MemoryStack, instance: E, func: (D, E, IntBuffer, B?) -> Int, creator: (Int) -> B): B {
		val count = stack.mallocInt(1)
		processResult(func(device, instance, count, null), "Failed to get number of $name")
		val buffer = creator(count[0])
		processResult(func(device, instance, count, buffer), "Failed to get $name")
		return buffer
	}

	@Throws(AssertionError::class)
	fun <D, E, B> getDeviceBuffer(device: D, instance: E, func: (D, E, IntBuffer, B?) -> Unit, creator: (Int) -> B): B {
		MemoryStack.stackPush().use { stack ->
			return getStackDeviceBuffer(device, stack, instance, func, creator)
		}
	}

	@Throws(AssertionError::class)
	fun <D, E, B> getStackDeviceBuffer(device: D, stack: MemoryStack, instance: E, func: (D, E, IntBuffer, B?) -> Unit, creator: (Int) -> B): B {
		val sizeBuffer = stack.mallocInt(1)
		func(device, instance, sizeBuffer, null)
		val buffer = creator(sizeBuffer[0])
		func(device, instance, sizeBuffer, buffer)
		return buffer
	}

	fun allocateLayerBuffer(layers: Array<String>, stack: MemoryStack): PointerBuffer {
		val availableLayers = getAvailableLayers(stack)
		val usedLayers = layers.filter { availableLayers.contains(it) }
		val enabledLayerNames = stack.mallocPointer(usedLayers.size)
		for (layer in usedLayers) {
			enabledLayerNames.put(stack.UTF8(layer, true))
		}
		return enabledLayerNames.flip()
	}

	fun getAvailableLayers(stack: MemoryStack): Set<String> {
		val set = mutableSetOf<String>()
		val buffer = stack.mallocInt(1)
		VK10.vkEnumerateInstanceLayerProperties(buffer, null)
		val count = buffer[0]

		if (count > 0) {
			val instanceLayers = VkLayerProperties.malloc(count, stack)
			VK10.vkEnumerateInstanceLayerProperties(buffer, instanceLayers)
			for (i in 0 until count) set.add(instanceLayers[i].layerNameString())
		}
		return set
	}

	fun createInstance(debug: Boolean): VkInstance {
		val extensions = arrayOf(
			EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME
		)

		val layers = arrayOf(
			"VK_LAYER_KHRONOS_validation",
		)

		val requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions() ?: throw Error("Missing required extensions")

		MemoryStack.stackPush().use { stack ->
			val enabledExtensionNames = stack.mallocPointer(requiredExtensions.remaining() + extensions.size)
			enabledExtensionNames.put(requiredExtensions)
			val pExtensions = extensions.map { stack.UTF8(it) }
			for (extension in pExtensions) enabledExtensionNames.put(extension)
			enabledExtensionNames.flip()

			val enabledLayerNames: PointerBuffer? = if (debug) allocateLayerBuffer(layers, stack) else null

			val appInfo = VkApplicationInfo.calloc(stack)
				.`sType$Default`()
				.apiVersion(VK14.VK_API_VERSION_1_3)

			val createInfo = VkInstanceCreateInfo.calloc(stack)
				.`sType$Default`()
				.pApplicationInfo(appInfo)
				.ppEnabledExtensionNames(enabledExtensionNames)
				.ppEnabledLayerNames(enabledLayerNames)

			val pointer = stack.mallocPointer(1)
			processResult(VK10.vkCreateInstance(createInfo, null, pointer), "Failed to create Vulkan Instance")

			return VkInstance(pointer.get(0), createInfo)
		}
	}

	fun setupDebugger(instance: VkInstance, severity: Int, callback: VkDebugUtilsMessengerCallbackEXTI): Long {
		MemoryStack.stackPush().use { stack ->
			val b = stack.mallocLong(1)
			val createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack).`sType$Default`()
				.messageSeverity(severity)
				.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
				.pfnUserCallback(callback)
			processResult(EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, b), "Failed to link debug callback")
			return b[0]
		}
	}

	fun getPhysicalDevices(instance: VkInstance): List<VulkanPhysicalDevice> {
		MemoryStack.stackPush().use { stack ->
			val physicalDevices = getStackBuffer("GPUs", stack, instance, VK10::vkEnumeratePhysicalDevices, stack::mallocPointer)
			return physicalDevices.map { VulkanPhysicalDevice(instance, it) }
		}
	}

	fun createSurface(instance: VkInstance, window: WindowI): VulkanSurface {
		MemoryStack.stackPush().use { stack ->
			val buffer = stack.mallocLong(1)
			processResult(GLFWVulkan.glfwCreateWindowSurface(instance, window.windowHandle, null, buffer), "Failed to create Surface")
			return VulkanSurface(instance, buffer[0])
		}
	}

	fun createSwapchain(
		device: VulkanDevice,
		surface: VulkanSurface,
		oldSwapchain: VulkanSwapchainHandler?,
		newWidth: Int,
		newHeight: Int,
		colourFormat: Int,
		colourSpace: Int
	): VulkanSwapchainHandler {

		MemoryStack.stackPush().use { stack ->
			val surfaceCaps = VkSurfaceCapabilitiesKHR.calloc(stack)
			processResult(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.device.physicalDevice, surface.handle, surfaceCaps), "Failed to get physical device surface capabilities")

			var numberImages = surfaceCaps.minImageCount()
			if (surfaceCaps.maxImageCount() in 1..<numberImages) numberImages = surfaceCaps.maxImageCount()

			val currentExtent = surfaceCaps.currentExtent()
			var width: Int = currentExtent.width()
			var height: Int = currentExtent.height()

			if (width == -1 || height == -1) {
				width = newWidth
				height = newHeight
			}

			val preTransform =
				if (surfaceCaps.supportedTransforms() and KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR != 0) KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR
				else surfaceCaps.currentTransform()

			val swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
				.`sType$Default`()
				.surface(surface.handle)
				.minImageCount(numberImages)
				.imageFormat(colourFormat)
				.imageColorSpace(colourSpace)
				.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.preTransform(preTransform)
				.imageArrayLayers(1)
				.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
				.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR)
				.oldSwapchain(oldSwapchain?.handle ?: VK10.VK_NULL_HANDLE)
				.clipped(true)
				.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
				.imageExtent(VkExtent2D.calloc(stack).set(width, height))

			val longBuffer = MemoryUtil.memAllocLong(1)
			processResult(KHRSwapchain.vkCreateSwapchainKHR(device.device, swapchainCreateInfo, null, longBuffer), "Failed to create Swapchain")

			oldSwapchain?.delete()
			val swapchainImages = getStackDeviceBuffer(device.device, stack, longBuffer[0], KHRSwapchain::vkGetSwapchainImagesKHR, stack::mallocLong)

			val viewCreateInfo = VkImageViewCreateInfo.calloc(stack)
				.`sType$Default`()
				.format(colourFormat)
				.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
				.subresourceRange(VkStructs.createImageRange(stack, VK10.VK_IMAGE_ASPECT_COLOR_BIT))

			val viewBuffer = stack.mallocLong(1)
			val format = TextureFormat.fromVulkanConst(colourFormat) ?: throw Error("Unknown Colour Format: $colourFormat")
			val images = swapchainImages.mapIndexed { index, image ->
				viewCreateInfo.image(image)
				processResult(VK10.vkCreateImageView(device.device, viewCreateInfo, null, viewBuffer), "Failed to create Image View")
				VulkanSwapchainImage(device, image, viewBuffer.get(0), format, width, height)
			}

			return VulkanSwapchainHandler(device, longBuffer, images)
		}
	}

	fun createVmaImage(device: VulkanDevice, type: Int, format: TextureFormat, usage: Int, aspect: Int, size: Vec3i): Triple<Long, Long, Long> {
		val tiling = VK10.VK_IMAGE_TILING_OPTIMAL
		val flags = 0
		val samples = 1 //if(type == VK10.VK_IMAGE_TYPE_2D && (usage and VK10.VK_IMAGE_USAGE_SAMPLED_BIT) > 0) VK10.VK_SAMPLE_COUNT_4_BIT else VK10.VK_SAMPLE_COUNT_1_BIT

		MemoryStack.stackPush().use { stack ->
			val extents = VkExtent3D.calloc(stack).set(size)

			val imageCreateInfo = VkStructs.createImageInfo(stack, type, format.vulkan, extents, samples, tiling, usage, flags)
			val vmaInfo = VmaAllocationCreateInfo.calloc(stack)
				.usage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
				.requiredFlags(VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)

			val longBuffer = stack.mallocLong(1)
			val pointerBuffer = stack.mallocPointer(1)
			var err = Vma.vmaCreateImage(device.allocator, imageCreateInfo, vmaInfo, longBuffer, pointerBuffer, null)
			val handle = longBuffer[0]
			processResult(err, "Failed to create Vulkan Image")

			val viewCreateInfo = VkStructs.createImageViewInfo(stack, type, handle, format.vulkan, VkStructs.createImageRange(stack, aspect, 0, 1, 0, 1))
			err = VK10.vkCreateImageView(device.device, viewCreateInfo, null, longBuffer)
			processResult(err, "Failed to create Vulkan Image View")
			return Triple(handle, longBuffer[0], pointerBuffer[0])
		}
	}

	fun createImage(device: VulkanDevice, id: String, format: TextureFormat, usage: Int, aspect: Int, size: Vec2i, parameters: TextureParameters = TextureParameters()): VulkanImage2D {
		val (handle, view, allocation) = createVmaImage(device, VK10.VK_IMAGE_TYPE_2D, format, usage, aspect, Vec3i(size, 1))
		device.nameObject(handle, VK10.VK_OBJECT_TYPE_IMAGE, "$id image")
		device.nameObject(view, VK10.VK_OBJECT_TYPE_IMAGE_VIEW, "$id image view")
		return VulkanImage2D(device, id, handle, view, format, allocation, size.x, size.y, parameters)
	}

	fun createImage3D(device: VulkanDevice, id: String, format: TextureFormat, usage: Int, aspect: Int, size: Vec3i, parameters: TextureParameters = TextureParameters()): VulkanImage3D {
		val (handle, view, allocation) = createVmaImage(device, VK10.VK_IMAGE_TYPE_3D, format, usage, aspect, size)
		device.nameObject(handle, VK10.VK_OBJECT_TYPE_IMAGE, "$id image")
		device.nameObject(view, VK10.VK_OBJECT_TYPE_IMAGE_VIEW, "$id image view")
		return VulkanImage3D(device, id, handle, view, format, allocation, size.x, size.y, size.z, parameters)
	}

	fun createPipelineLayout(
		device: VulkanDevice,
		stack: MemoryStack,
		descriptorLayouts: List<VulkanDescriptorLayout>,
		createInfo: VkPipelineLayoutCreateInfo,
		pushConstants: VulkanPushConstantManager
	): VulkanPipelineLayout {
		val buf = stack.mallocLong(1)
		processResult(VK10.vkCreatePipelineLayout(device.device, createInfo, null, buf), "Failed to create Pipeline Layout")
		return VulkanPipelineLayout(device, buf[0], descriptorLayouts, pushConstants)
	}

	fun createPipelineLayout(
		device: VulkanDevice,
		stack: MemoryStack,
		descriptorLayout: VulkanDescriptorLayout?,
		pushConstants: VulkanPushConstantManager
	): VulkanPipelineLayout {
		val pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack)
			.`sType$Default`()
			.pSetLayouts(descriptorLayout?.pointer)
			.pPushConstantRanges(pushConstants.createRanges(stack))
		return createPipelineLayout(device, stack, if (descriptorLayout == null) emptyList() else listOf(descriptorLayout), pipelineLayoutCreateInfo, pushConstants)
	}

	fun createPipelineLayout(
		device: VulkanDevice,
		stack: MemoryStack,
		descriptorLayouts: List<VulkanDescriptorLayout>,
		pushConstants: VulkanPushConstantManager
	): VulkanPipelineLayout {

		val maxSet = descriptorLayouts.maxOfOrNull(VulkanDescriptorLayout::set) ?: -1
		val buffer = stack.mallocLong(maxSet + 1)
		for (layout in descriptorLayouts) buffer.put(layout.set, layout.handle)

		val pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack)
			.`sType$Default`()
			.pSetLayouts(buffer)
			.pPushConstantRanges(pushConstants.createRanges(stack))

		return createPipelineLayout(device, stack, descriptorLayouts, pipelineLayoutCreateInfo, pushConstants)
	}
}