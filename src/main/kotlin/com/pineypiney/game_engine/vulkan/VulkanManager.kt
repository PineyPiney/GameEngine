package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.window.WindowI
import glm_.vec3.Vec3i
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.EXTDebugReport
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDebugReportCallbackEXT

class VulkanManager(window: WindowI) {

	val instance = VkUtil.createInstance(true)

	val errorHandle = VkUtil.setupDebugger(instance, EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT or EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT, ::errorCallback)
	val debugHandle = VkUtil.setupDebugger(instance, EXTDebugReport.VK_DEBUG_REPORT_INFORMATION_BIT_EXT or EXTDebugReport.VK_DEBUG_REPORT_DEBUG_BIT_EXT, ::debugCallback)

	val gpu = VkUtil.getPhysicalDevices(instance).first()
	val device = gpu.createDevice()
	val surface = VkUtil.createSurface(instance, window)
	val colourFormatSpace = VkUtil.getColourFormatAndSpace(gpu, surface.handle)

	val descriptorAllocator = VulkanDescriptorAllocator(device)

	val commands = Array(2) { PoolAndBuffer.create(device) }
	val queue = VkUtil.createQueue(device)
	val swapchain = VkUtil.createSwapchain(device, surface, null, window.width, window.height, colourFormatSpace.first, colourFormatSpace.second)

	val renderFence = VkUtil.createFence(device, VK10.VK_FENCE_CREATE_SIGNALED_BIT)
	val swapchainSemaphore = VkUtil.createSemaphore(device, 0)
	val renderSemaphore = VkUtil.createSemaphore(device, 0)

	// The image that is drawn to each frame, it is then blitted onto the swapchain's current image
	val drawImage = VkUtil.createImage(device, VK10.VK_IMAGE_TYPE_2D, VK10.VK_FORMAT_R16G16B16A16_SFLOAT, Vec3i(window.size, 1))

	val pLayout = MemoryUtil.memAllocLong(1)

	init {
		pLayout.put(descriptorAllocator.createDescriptorLayout()).flip()
	}

	val descriptorSet = descriptorAllocator.allocate(pLayout)

	val deletionQueue = DeletionQueue(this)

	init {
		deletionQueue
			.push { VK10.vkDestroyInstance(instance, null) }
			.push { EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, debugHandle, null) }
			.push { EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, errorHandle, null) }
			.pushAll(device, surface, descriptorAllocator)
			.pushAll(swapchain, renderFence, swapchainSemaphore, renderSemaphore, drawImage)
			.pushArray(commands)
			.push(pLayout[0], VK10::vkDestroyDescriptorSetLayout)
	}

	@Suppress("unused")
	fun errorCallback(flags: Int, objectType: Int, obj: Long, location: Long, messageCode: Int, pLayerPrefix: Long, pMessage: Long, pUserData: Long): Int {
		GameEngineI.logger.error("Vulkan Error Occurred: " + VkDebugReportCallbackEXT.getString(pMessage))
		return 0
	}

	@Suppress("unused")
	fun debugCallback(flags: Int, objectType: Int, obj: Long, location: Long, messageCode: Int, pLayerPrefix: Long, pMessage: Long, pUserData: Long): Int {
//		GameEngineI.logger.debug("Vulkan Debugging: " + VkDebugReportCallbackEXT.getString(pMessage))
		return 0
	}

	fun cleanUp() {
		swapchain.device.waitIdle()
		deletionQueue.flush()
	}
}