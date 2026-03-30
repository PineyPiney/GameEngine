package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.window.WindowI
import kool.free
import kool.indices
import kool.map
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.CustomBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Struct
import org.lwjgl.vulkan.*
import java.nio.IntBuffer
import java.nio.LongBuffer

object VkUtil {

	fun translateVulkanResult(result: Int): String {
		return when (result) {
			VK10.VK_SUCCESS -> "Command successfully completed."
			VK10.VK_NOT_READY -> "A fence or query has not yet completed."
			VK10.VK_TIMEOUT -> "A wait operation has not completed in the specified time."
			VK10.VK_EVENT_SET -> "An event is signaled."
			VK10.VK_EVENT_RESET -> "An event is unsignaled."
			VK10.VK_INCOMPLETE -> "A return array was too small for the result."
			KHRSwapchain.VK_SUBOPTIMAL_KHR -> "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully."
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
	fun <E, B : CustomBuffer<B>> getBuffer(name: String, instance: E, func: (E, IntArray, B?) -> Int, creator: (Int) -> B): B {
		val array = IntArray(1)
		processError(func(instance, array, null), "Failed to get number of $name")
		val buffer = creator(array[0])
		processError(func(instance, array, buffer), "Failed to get $name") {
			buffer.free()
		}
		return buffer
	}

	@Throws(AssertionError::class)
	fun <E, B> getBuffer(instance: E, func: (E, IntArray, B?) -> Unit, creator: (Int) -> B): B {
		val array = IntArray(1)
		func(instance, array, null)
		val buffer = creator(array[0])
		func(instance, array, buffer)
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
		return enabledLayerNames
	}

	fun getAvailableLayers(): Set<String> {
		val set = mutableSetOf<String>()
		val array = IntArray(1)
		VK10.vkEnumerateInstanceLayerProperties(array, null)
		val count = array[0]

		if (count > 0) {
			try {
				val stack = MemoryStack.stackPush()
				val instanceLayers = VkLayerProperties.malloc(count, stack)
				VK10.vkEnumerateInstanceLayerProperties(array, instanceLayers)
				for (i in 0 until count) set.add(instanceLayers[i].layerNameString())
			} catch (_: Throwable) {
			}
		}
		return set
	}

	fun createInstance(debug: Boolean): VkInstance {

		val layers = arrayOf(
			"VK_LAYER_LUNARG_standard_validation",
			"VK_LAYER_KHRONOS_validation",
		)

		val appInfo = VkApplicationInfo.calloc()
			.`sType$Default`()
			.apiVersion(VK14.VK_API_VERSION_1_3)

		val requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions() ?: throw Error("Missing required extensions")
		val enabledExtensionNames = MemoryUtil.memAllocPointer(requiredExtensions.remaining() + 1)
		enabledExtensionNames.put(requiredExtensions)
		val debugExtension = MemoryUtil.memUTF8(EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME)
		enabledExtensionNames.put(debugExtension)
		enabledExtensionNames.flip()

		val enabledLayerNames: PointerBuffer? = if (debug) allocateLayerBuffer(layers) else null

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
		debugExtension.free()
		enabledExtensionNames.free()
		enabledLayerNames?.free()
		appInfo.pApplicationName()?.free()
		appInfo.pEngineName()?.free()
		appInfo.free()

		return instance
	}

	fun getPhysicalDevices(instance: VkInstance): List<VulkanPhysicalDevice> {
		val physicalDevices = getBuffer("GPUs", instance, VK10::vkEnumeratePhysicalDevices, MemoryUtil::memAllocPointer)

		val devices = physicalDevices.map { VulkanPhysicalDevice(instance, it) }
		physicalDevices.free()

		return devices
	}

	fun getDevice(physicalDevice: VkPhysicalDevice): VulkanDevice {
		val properties = getBuffer(physicalDevice, VK10::vkGetPhysicalDeviceQueueFamilyProperties, VkQueueFamilyProperties::calloc)
		var index = properties.indexOfFirst { property ->
			property.queueFlags() and VK10.VK_QUEUE_GRAPHICS_BIT != 0
		}
		if (index == -1) index = properties.capacity()
		properties.free()

		val priorities = MemoryUtil.memAllocFloat(1).put(0f)
		priorities.flip()

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
			.pQueueCreateInfos(queueCreateInfo)
			.ppEnabledExtensionNames(extensions)

		val pointer = MemoryUtil.memAllocPointer(1)
		processError(VK10.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pointer), "Failed to create Vulkan device") {
			pointer.free()
		}

		val memoryProperties = VkPhysicalDeviceMemoryProperties.calloc()
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties)

		val device = VulkanDevice(VkDevice(pointer.get(), physicalDevice, deviceCreateInfo), index, memoryProperties)
		pointer.free()
		deviceCreateInfo.free()
		swapchainExt.free()
		extensions.free()
		priorities.free()
		return device
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
			.preTransform(preTransform)
			.imageArrayLayers(1)
			.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
			.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR)
			.oldSwapchain(oldSwapchain?.handle ?: VK10.VK_NULL_HANDLE)
			.clipped(true)
			.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
			.imageExtent(VkExtent2D.calloc().width(width).height(height))

		val longBuffer = MemoryUtil.memAllocLong(1)
		val err = KHRSwapchain.vkCreateSwapchainKHR(device.device, swapchainCreateInfo, null, longBuffer)
		swapchainCreateInfo.free()

		processError(err, "Failed to create Swapchain")

		oldSwapchain?.delete()

		val swapchainImages = getBuffer(device.device, longBuffer[0], KHRSwapchain::vkGetSwapchainImagesKHR, MemoryUtil::memAllocLong)
		val images = LongArray(swapchainImages.capacity())
		val imageViews = LongArray(images.size)

		val viewCreateInfo = VkImageViewCreateInfo.calloc()
			.`sType$Default`()
			.format(colourFormat)
			.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
			.subresourceRange { range ->
				range
					.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
					.levelCount(1)
			}

		val viewBuffer = MemoryUtil.memAllocLong(1)
		for (i in images.indices) {
			images[i] = swapchainImages[i]
			viewCreateInfo.image(images[i])
			processError(VK10.vkCreateImageView(device.device, viewCreateInfo, null, viewBuffer), "Failed to create Image View")
			imageViews[i] = viewBuffer.get(0)
		}

		viewCreateInfo.free()
		viewBuffer.free()
		swapchainImages.free()

		return VulkanSwapchainHandler(device, longBuffer, images, imageViews)
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
}