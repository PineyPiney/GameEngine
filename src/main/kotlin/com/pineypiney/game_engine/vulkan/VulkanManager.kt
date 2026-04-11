package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.EXTDebugReport
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDebugReportCallbackEXT

class VulkanManager(window: WindowI) {

	val deletionQueue = DeletionQueue(this)

	val instance = VkUtil.createInstance(true)

	val errorHandle = VkUtil.setupDebugger(instance, EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT or EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT, ::errorCallback)
	val debugHandle = VkUtil.setupDebugger(instance, EXTDebugReport.VK_DEBUG_REPORT_INFORMATION_BIT_EXT or EXTDebugReport.VK_DEBUG_REPORT_DEBUG_BIT_EXT, ::debugCallback)

	val gpu = VkUtil.getPhysicalDevices(instance).first()
	val device = gpu.createDevice(deletionQueue)
	val descriptorAllocator = VulkanDescriptorAllocator(device)
	val queue = VkUtil.createQueue(device)
	val pLayout = MemoryUtil.memAllocLong(1)

	init {
		pLayout.put(descriptorAllocator.createDescriptorLayout()).flip()
	}

	val descriptorSet = descriptorAllocator.allocate(pLayout)

	val submitter = VulkanImmediateSubmitter(this)


	val surface = VkUtil.createSurface(instance, window)
	val colourFormatSpace = VkUtil.getColourFormatAndSpace(gpu, surface)
	var swapchain = VkUtil.createSwapchain(device, surface, null, window.width, window.height, colourFormatSpace.first, colourFormatSpace.second)

	// The image that is drawn to each frame, it is then blitted onto the swapchain's current image
	val drawImage: VulkanImage
	val depthImage: VulkanImage

	init {

		val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
				VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
				VK10.VK_IMAGE_USAGE_STORAGE_BIT or
				VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT
		drawImage = VkUtil.createImage(device, VK10.VK_IMAGE_TYPE_2D, VK10.VK_FORMAT_R16G16B16A16_SFLOAT, usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, Vec3i(window.size, 1))
		depthImage =
			VkUtil.createImage(device, VK10.VK_IMAGE_TYPE_2D, VK10.VK_FORMAT_D32_SFLOAT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT, Vec3i(window.size, 1))
		VK10.VK_IMAGE_ASPECT_COLOR_BIT
	}

	fun updateSwapchain(size: Vec2i) {
		device.waitIdle()
		swapchain = VkUtil.createSwapchain(device, surface, swapchain, size.x, size.y, colourFormatSpace.first, colourFormatSpace.second)
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
		deletionQueue.flush()
		VK10.vkDestroyDescriptorSetLayout(device.device, pLayout[0], null)

		submitter.delete()
		drawImage.delete()
		depthImage.delete()
		swapchain.delete()
		descriptorAllocator.delete()
		surface.delete()
		device.delete()

		EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, debugHandle, null)
		EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, errorHandle, null)
		VK10.vkDestroyInstance(instance, null)
	}
}