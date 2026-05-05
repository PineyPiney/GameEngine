package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.VulkanDeletionQueue
import org.lwjgl.vulkan.EXTDebugReport
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDebugReportCallbackEXT

class VulkanManager {

	val instance = VkUtil.createInstance(true)

	val errorHandle = VkUtil.setupDebugger(instance, EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT or EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT, ::errorCallback)
	val debugHandle = VkUtil.setupDebugger(instance, EXTDebugReport.VK_DEBUG_REPORT_INFORMATION_BIT_EXT or EXTDebugReport.VK_DEBUG_REPORT_DEBUG_BIT_EXT, ::debugCallback)

	val gpu = VkUtil.getPhysicalDevices(instance).first()
	val device = gpu.createDevice()
	val queue = device.getQueue(0)

	val deletionQueue = VulkanDeletionQueue(device)

	val submitter = VulkanImmediateSubmitter(this)

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

		submitter.delete()
		device.delete()

		EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, debugHandle, null)
		EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, errorHandle, null)
		VK10.vkDestroyInstance(instance, null)
	}
}