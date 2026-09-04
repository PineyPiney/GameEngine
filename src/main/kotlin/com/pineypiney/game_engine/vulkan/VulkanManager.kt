package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.VulkanDeletionQueue
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT

class VulkanManager {

	val instance = VkUtil.createInstance(true)

	// https://www.lunarg.com/wp-content/uploads/2018/05/Vulkan-Debug-Utils_05_18_v1.pdf
	val errorHandle = VkUtil.setupDebugger(instance, EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT, ::errorCallback)

	val gpu = VkUtil.getPhysicalDevices(instance).first()
	val device = gpu.createDevice()
	val queue = device.getQueue(0)

	val deletionQueue = VulkanDeletionQueue(device)

	val submitter = VulkanImmediateSubmitter(this)

	val drawFormat = TextureFormat.RGBA8
	val depthFormat = TextureFormat.DEPTH24_STENCIL8

	init {
		INSTANCE = this
		DeletionQueue.setGlobalQueue(deletionQueue)


		device.nameObject(instance.address(), VK10.VK_OBJECT_TYPE_INSTANCE, "Vulkan Instance")
		device.nameObject(gpu.physicalDevice.address(), VK10.VK_OBJECT_TYPE_PHYSICAL_DEVICE, gpu.name)
		device.nameObject(device.device.address(), VK10.VK_OBJECT_TYPE_DEVICE, device.physicalDevice.name + " Vulkan Device")
	}

	@Suppress("unused")
	fun errorCallback(severity: Int, type: Int, callbackData: Long, userData: Long): Int {
		val data = VkDebugUtilsMessengerCallbackDataEXT.create(callbackData)
		when (severity) {
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> {
				GameEngineI.logger.error("Vulkan Error Occurred: " + data.pMessageString())
			}
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> {
				GameEngineI.logger.error("Vulkan Warning Occurred: " + data.pMessageString())
			}
		}
		return 0
	}

	fun cleanUp() {
		deletionQueue.flush()

		submitter.delete()
		device.delete()

		EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, errorHandle, null)
		VK10.vkDestroyInstance(instance, null)
	}

	companion object {
		lateinit var INSTANCE: VulkanManager
	}
}