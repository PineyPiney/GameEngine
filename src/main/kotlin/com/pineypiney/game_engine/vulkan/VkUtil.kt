package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import kool.free
import kool.indices
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

	fun isError(err: Int) = err != VK10.VK_SUCCESS

	@Throws(AssertionError::class)
	fun processError(err: Int, message: String) {
		if (err != VK10.VK_SUCCESS) {
			throw AssertionError("$message: ${translateVulkanResult(err)}")
		}
	}

	@Throws(AssertionError::class)
	fun processError(err: Int, message: String, free: () -> Unit) {
		if (err != VK10.VK_SUCCESS) {
			free()
			throw AssertionError("$message: ${translateVulkanResult(err)}")
		}
	}

	@Throws(AssertionError::class)
	fun <S : Struct<S>> getLong(name: String, device: VulkanDevice, struct: S, func: (VkDevice, S, VkAllocationCallbacks?, LongBuffer) -> Int): Long {
		val longBuffer = MemoryUtil.memAllocLong(1)
		val err = func(device.device, struct, null, longBuffer)

		val long = longBuffer.get()
		struct.free()
		longBuffer.free()

		processError(err, "Failed to create $name")

		return long
	}

	@Throws(AssertionError::class)
	fun <S : Struct<S>> allocatePointer(name: String, device: VulkanDevice, struct: S, func: (VkDevice, S, PointerBuffer) -> Int): Long {
		val pointer = MemoryUtil.memAllocPointer(1)
		val err = func(device.device, struct, pointer)

		val long = pointer.get()
		struct.free()
		pointer.free()

		processError(err, "Failed to allocate $name")

		return long
	}

	@Throws(AssertionError::class)
	fun <E, B : CustomBuffer<B>> getBuffer(name: String, instance: E, func: (E, IntBuffer, B?) -> Int, creator: (Int) -> B): B {
		val count = MemoryUtil.memAllocInt(1)
		processError(func(instance, count, null), "Failed to get number of $name")
		val buffer = creator(count[0])
		processError(func(instance, count, buffer), "Failed to get $name") {
			buffer.free()
		}
		count.free()
		return buffer
	}

	@Throws(AssertionError::class)
	fun <E, B> getBuffer(instance: E, func: (E, IntBuffer, B?) -> Unit, creator: (Int) -> B): B {
		val count = MemoryUtil.memAllocInt(1)
		func(instance, count, null)
		val buffer = creator(count[0])
		func(instance, count, buffer)
		count.free()
		return buffer
	}

	@Throws(AssertionError::class)
	fun <D, E, B> getBuffer(name: String, device: D, instance: E, func: (D, E, IntBuffer, B?) -> Int, creator: (Int) -> B): B {
		val sizeBuffer = MemoryUtil.memAllocInt(1)
		processError(func(device, instance, sizeBuffer, null), "Failed to get number of $name") {
			sizeBuffer.free()
		}
		val buffer = creator(sizeBuffer[0])
		val err = func(device, instance, sizeBuffer, buffer)
		sizeBuffer.free()
		processError(err, "Failed to get $name")
		return buffer
	}

	@Throws(AssertionError::class)
	fun <D, E, B> getBuffer(device: D, instance: E, func: (D, E, IntBuffer, B?) -> Unit, creator: (Int) -> B): B {
		val sizeBuffer = MemoryUtil.memAllocInt(1)
		func(device, instance, sizeBuffer, null)
		val buffer = creator(sizeBuffer[0])
		func(device, instance, sizeBuffer, buffer)
		sizeBuffer.free()
		return buffer
	}

	fun allocateLayerBuffer(layers: Array<String>): PointerBuffer {
		val availableLayers = getAvailableLayers()
		val usedLayers = layers.filter { availableLayers.contains(it) }
		val enabledLayerNames = MemoryUtil.memAllocPointer(usedLayers.size)
		for (layer in usedLayers) {
			enabledLayerNames.put(MemoryUtil.memUTF8(layer))
		}
		return enabledLayerNames.flip()
	}

	fun getAvailableLayers(): Set<String> {
		val set = mutableSetOf<String>()
		val buffer = MemoryUtil.memAllocInt(1)
		VK10.vkEnumerateInstanceLayerProperties(buffer, null)
		val count = buffer[0]

		if (count > 0) {
			try {
				val stack = MemoryStack.stackPush()
				val instanceLayers = VkLayerProperties.malloc(count, stack)
				VK10.vkEnumerateInstanceLayerProperties(buffer, instanceLayers)
				for (i in 0 until count) set.add(instanceLayers[i].layerNameString())
			} catch (_: Throwable) {
			}
		}
		return set
	}

	fun createInstance(debug: Boolean): VkInstance {

		val extensions = arrayOf(
			EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME,
//			KHRBufferDeviceAddress.VK_KHR_BUFFER_DEVICE_ADDRESS_EXTENSION_NAME
		)

		val layers = arrayOf(
			"VK_LAYER_KHRONOS_validation",
		)

		val requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions() ?: throw Error("Missing required extensions")
		val enabledExtensionNames = MemoryUtil.memAllocPointer(requiredExtensions.remaining() + extensions.size)
		enabledExtensionNames.put(requiredExtensions)
		val pExtensions = extensions.map { MemoryUtil.memUTF8(it) }
		for (extension in pExtensions) enabledExtensionNames.put(extension)
		enabledExtensionNames.flip()

		val enabledLayerNames: PointerBuffer? = if (debug) allocateLayerBuffer(layers) else null

		val appInfo = VkApplicationInfo.calloc()
			.`sType$Default`()
			.apiVersion(VK14.VK_API_VERSION_1_3)

		val createInfo = VkInstanceCreateInfo.calloc()
			.`sType$Default`()
			.pApplicationInfo(appInfo)
			.ppEnabledExtensionNames(enabledExtensionNames)
			.ppEnabledLayerNames(enabledLayerNames)

		val pointer = MemoryUtil.memAllocPointer(1)
		val err = VK10.vkCreateInstance(createInfo, null, pointer)
		val handle = pointer.get(0)
		pointer.free()
		processError(err, "Failed to create Vulkan Instance")

		val instance = VkInstance(handle, createInfo)

		createInfo.free()
		for (extension in pExtensions) extension.free()
		enabledExtensionNames.free()
		enabledLayerNames?.free()
		appInfo.pApplicationName()?.free()
		appInfo.pEngineName()?.free()
		appInfo.free()

		return instance
	}

	fun setupDebugger(instance: VkInstance, flags: Int, callback: VkDebugReportCallbackEXTI): Long {
		val debuggerInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
			.`sType$Default`()
			.pfnCallback(callback)
			.flags(flags)
		val b = MemoryUtil.memAllocLong(1)
		processError(EXTDebugReport.vkCreateDebugReportCallbackEXT(instance, debuggerInfo, null, b), "Failed to link debug callback")
		val handle = b[0]
		b.free()
		debuggerInfo.free()
		return handle
	}

	fun getPhysicalDevices(instance: VkInstance): List<VulkanPhysicalDevice> {
		val physicalDevices = getBuffer("GPUs", instance, VK10::vkEnumeratePhysicalDevices, MemoryUtil::memAllocPointer)

		val devices = physicalDevices.map { VulkanPhysicalDevice(instance, it) }
		physicalDevices.free()

		return devices
	}

	fun createSurface(instance: VkInstance, window: WindowI): VulkanSurface {
		val buffer = MemoryUtil.memAllocLong(1)
		processError(GLFWVulkan.glfwCreateWindowSurface(instance, window.windowHandle, null, buffer), "Failed to create Surface") { buffer.free() }
		val handle = buffer.get()
		buffer.free()
		return VulkanSurface(instance, handle)
	}

	fun createCommandPool(device: VulkanDevice, familyIndex: Int = device.queueFamilyIndex, flags: Int = VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT): Long {
		val poolCreateInfo = VkCommandPoolCreateInfo.calloc()
			.`sType$Default`()
			.queueFamilyIndex(familyIndex)
			.flags(flags)

		return createCommandPool(device, poolCreateInfo)
	}

	fun createCommandPool(device: VulkanDevice, createInfo: VkCommandPoolCreateInfo): Long {
		return getLong("Command Pool", device, createInfo, VK10::vkCreateCommandPool)
	}

	fun createCommandBuffer(device: VulkanDevice, commandPool: Long): VkCommandBuffer {
		val allocateInfo = VkCommandBufferAllocateInfo.calloc()
			.`sType$Default`()
			.commandPool(commandPool)
			.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandBufferCount(1)


		val handle = allocatePointer("", device, allocateInfo, VK10::vkAllocateCommandBuffers)
		return VkCommandBuffer(handle, device.device)
	}

	fun getColourFormatAndSpace(device: VulkanPhysicalDevice, surface: Long): Pair<Int, Int> {
		val properties = getBuffer(device.physicalDevice, VK10::vkGetPhysicalDeviceQueueFamilyProperties, VkQueueFamilyProperties::calloc)

		// Find which queues support presentation
		val canPresent = MemoryUtil.memAllocInt(properties.capacity())
		for (i in canPresent.indices) {
			canPresent.position(i)
			processError(KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device.physicalDevice, i, surface, canPresent), "Failed to get physical device's surface support")
		}

		// Search for a queue that supports both graphics and presentation
		var graphicsQIndex = Int.MAX_VALUE
		var presentQIndex = Int.MAX_VALUE
		for (i in canPresent.indices) {
			if (properties[i].queueFlags() and VK10.VK_QUEUE_GRAPHICS_BIT != 0) {
				// If this queue supports both graphics and presentation then use this one
				if (canPresent[i] == VK10.VK_TRUE) {
					graphicsQIndex = i
					presentQIndex = i
					break
				} else if (graphicsQIndex == Int.MAX_VALUE) graphicsQIndex = i
			} else if (canPresent[i] == VK10.VK_TRUE) presentQIndex = i
		}
		properties.free()

		if (graphicsQIndex == Int.MAX_VALUE) throw AssertionError("No Graphics Queue found")
		else if (presentQIndex == Int.MAX_VALUE) throw AssertionError("No Presentation Queue found")
		else if (presentQIndex != graphicsQIndex) throw AssertionError("No Queue found that support Graphics and Presentation")

		val formats = getBuffer("Physical Device Surface Formats", device.physicalDevice, surface, KHRSurface::vkGetPhysicalDeviceSurfaceFormatsKHR, VkSurfaceFormatKHR::calloc)
		val colourFormat = if (formats.capacity() == 1 && formats[0].format() == VK10.VK_FORMAT_UNDEFINED) VK10.VK_FORMAT_B8G8R8A8_UNORM
		else formats[0].format()
		val colourSpace = formats[0].colorSpace()
		formats.free()

		return colourFormat to colourSpace
	}

	fun createQueue(device: VulkanDevice): VkQueue {
		val pointer = MemoryUtil.memAllocPointer(1)
		VK10.vkGetDeviceQueue(device.device, device.queueFamilyIndex, 0, pointer)
		val queue = pointer.get()
		pointer.free()
		return VkQueue(queue, device.device)
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

		val surfaceCaps = VkSurfaceCapabilitiesKHR.calloc()
		processError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.device.physicalDevice, surface.handle, surfaceCaps), "Failed to get physical device surface capabilities")

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
		surfaceCaps.free()

		val swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc()
			.`sType$Default`()
			.surface(surface.handle)
			.minImageCount(numberImages)
			.imageFormat(colourFormat)
			.imageColorSpace(colourSpace)
			.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
			.preTransform(preTransform)
			.imageArrayLayers(1)
			.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
			.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR)
			.oldSwapchain(oldSwapchain?.handle ?: VK10.VK_NULL_HANDLE)
			.clipped(true)
			.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
			.imageExtent(VkExtent2D.calloc().set(width, height))

		val longBuffer = MemoryUtil.memAllocLong(1)
		val err = KHRSwapchain.vkCreateSwapchainKHR(device.device, swapchainCreateInfo, null, longBuffer)
		swapchainCreateInfo.free()

		processError(err, "Failed to create Swapchain")

		oldSwapchain?.delete()

		val swapchainImages = getBuffer(device.device, longBuffer[0], KHRSwapchain::vkGetSwapchainImagesKHR, MemoryUtil::memAllocLong)

		val viewCreateInfo = VkImageViewCreateInfo.calloc()
			.`sType$Default`()
			.format(colourFormat)
			.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
			.subresourceRange(VkStructs.createImageRange(VK10.VK_IMAGE_ASPECT_COLOR_BIT))

		val viewBuffer = MemoryUtil.memAllocLong(1)
		val images = swapchainImages.mapIndexed { index, image ->
			viewCreateInfo.image(image)
			processError(VK10.vkCreateImageView(device.device, viewCreateInfo, null, viewBuffer), "Failed to create Image View")
			VulkanSwapchainImage(device, image, viewBuffer.get(0), Vec2i(width, height))
		}

		viewCreateInfo.free()
		viewBuffer.free()
		swapchainImages.free()

		return VulkanSwapchainHandler(device, longBuffer, images)
	}

	fun createFenceInfo(flags: Int): VkFenceCreateInfo {
		return VkFenceCreateInfo.calloc()
			.`sType$Default`()
			.flags(flags)
	}

	fun createFence(device: VulkanDevice, flags: Int): VulkanFence {
		val createInfo = createFenceInfo(flags)
		return createFence(device, createInfo)
	}

	fun createFence(device: VulkanDevice, createInfo: VkFenceCreateInfo): VulkanFence {
		return VulkanFence(device, getLong("Vulkan Fence", device, createInfo, VK10::vkCreateFence))
	}

	fun createSemaphoreInfo(flags: Int): VkSemaphoreCreateInfo {
		return VkSemaphoreCreateInfo.calloc()
			.`sType$Default`()
			.flags(flags)
	}

	fun createSemaphore(device: VulkanDevice, flags: Int): VulkanSemaphoreHandler {
		val createInfo = createSemaphoreInfo(flags)
		val handler = createSemaphore(device, createInfo)
		createInfo.free()
		return handler
	}

	fun createSemaphore(device: VulkanDevice, createInfo: VkSemaphoreCreateInfo): VulkanSemaphoreHandler {
		val buffer = MemoryUtil.memAllocLong(1)
		VK10.vkCreateSemaphore(device.device, createInfo, null, buffer)
		return VulkanSemaphoreHandler(device, buffer)
	}

	fun createImage(device: VulkanDevice, type: Int, format: Int, size: Vec3i): VulkanImage {

		val extents = VkExtent3D.calloc().set(size)
		val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
				VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
				VK10.VK_IMAGE_USAGE_STORAGE_BIT or
				VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT

		val imageCreateInfo = VkStructs.createImageInfo(type, format, extents, VK10.VK_SAMPLE_COUNT_1_BIT, VK10.VK_IMAGE_TILING_OPTIMAL, usage)
		val vmaInfo = VmaAllocationCreateInfo.calloc()
			.usage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
			.requiredFlags(VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)

		val longBuffer = MemoryUtil.memAllocLong(1)
		val pointerBuffer = MemoryUtil.memAllocPointer(1)
		var err = Vma.vmaCreateImage(device.allocator, imageCreateInfo, vmaInfo, longBuffer, pointerBuffer, null)
		val handle = longBuffer[0]
		val allocation = pointerBuffer[0]
//		pointerBuffer.free()
		processError(err, "Failed to create Vulkan Image") {
//			longBuffer.free()
		}

		val viewCreateInfo = VkStructs.createImageViewInfo(type, handle, format, VkStructs.createImageRange(VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0, 1, 0, 1))
		err = VK10.vkCreateImageView(device.device, viewCreateInfo, null, longBuffer)
		val view = longBuffer[0]
//		longBuffer.free()
		processError(err, "Failed to create Vulkan Image View")

		return VulkanImage(device, handle, view, allocation, Vec2i(extents.width(), extents.height()))
	}

	fun createPipelineLayout(device: VulkanDevice, layouts: LongBuffer, constants: VkPushConstantRange.Buffer? = null): Long {

		val pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
			.`sType$Default`()
			.pSetLayouts(layouts)
			.pPushConstantRanges(constants)
		val buf = MemoryUtil.memAllocLong(1)
		processError(VK10.vkCreatePipelineLayout(device.device, pipelineLayoutCreateInfo, null, buf), "Failed to create Pipeline Layout")
		val layout = buf[0]
		buf.free()
		pipelineLayoutCreateInfo.free()
		return layout
	}

	fun createComputePipeline(device: VulkanDevice, module: Long, layout: Long): Long {
		val stageCreateInfo = VkPipelineShaderStageCreateInfo.calloc()
			.`sType$Default`()
			.stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
			.module(module)
			.pName(MemoryUtil.memUTF8("main"))
		val pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1)
			.`sType$Default`()
			.layout(layout)
			.stage(stageCreateInfo)

		val buf = MemoryUtil.memAllocLong(1)
		val err = VK10.vkCreateComputePipelines(device.device, 0L, pipelineCreateInfo, null, buf)
		val pipeline = buf[0]
		buf.free()
		pipelineCreateInfo.free()
		stageCreateInfo.free()
		processError(err, "Failed to create Compute Pipeline")
		return pipeline
	}
}