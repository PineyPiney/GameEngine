package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.vulkan.VulkanPresentRendering
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.VulkanResourceFactory
import com.pineypiney.game_engine.resources.textures.TextureFormat
import kool.iterator
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT
import org.lwjgl.vulkan.VkInstance

class VulkanManager(appName: String, glfw: Boolean) : ApiManager {

	val instance: VkInstance

	init {
	    val extensions = mutableListOf<Long>()
		MemoryStack.stackPush().use { stack ->
			extensions.add(MemoryUtil.memAddress0(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)))
			if(glfw) {
				val glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions() ?: throw RuntimeException("Missing required GLFW extensions")
				for(extension in glfwExtensions) extensions.add(extension)
			}

			val extensionsPtr = stack.mallocPointer(extensions.size)
			for(i in extensions.indices) extensionsPtr.put(extensions[i])
			instance = VkUtil.createInstance(appName, extensionsPtr.flip())
		}

	}

	// https://www.lunarg.com/wp-content/uploads/2018/05/Vulkan-Debug-Utils_05_18_v1.pdf
	val errorHandle = VkUtil.setupDebugger(instance, EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT, ::errorCallback)

	val gpu = VkUtil.getPhysicalDevices(instance).first()
	val device = gpu.createDevice()
	val queue = device.getQueue(0)

	val submitter = VulkanImmediateSubmitter(this)

	val drawFormat = TextureFormat.RGBA8
	val depthFormat = TextureFormat.DEPTH24_STENCIL8

	init {
		device.nameObject(instance.address(), VK10.VK_OBJECT_TYPE_INSTANCE, "Vulkan Instance")
		device.nameObject(gpu.physicalDevice.address(), VK10.VK_OBJECT_TYPE_PHYSICAL_DEVICE, gpu.name)
		device.nameObject(device.device.address(), VK10.VK_OBJECT_TYPE_DEVICE, device.physicalDevice.name + " Vulkan Device")
		device.nameObject(errorHandle, EXTDebugUtils.VK_OBJECT_TYPE_DEBUG_UTILS_MESSENGER_EXT, "Error Messenger")
	}

	@Suppress("unused")
	fun errorCallback(severity: Int, type: Int, callbackData: Long, userData: Long): Int {
		val data = VkDebugUtilsMessengerCallbackDataEXT.create(callbackData)
		when (severity) {
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> {
				GameEngineI.logger.error("Vulkan Error Occurred: " + data.pMessageString())
			}
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> {
				GameEngineI.logger.warn("Vulkan Warning Occurred: " + data.pMessageString())
			}
		}
		return 0
	}

	override fun createResourceFactory(): ResourceFactory = VulkanResourceFactory(this)

	override fun createPresentingApi(renderer: WindowRendererI<*>, name: String): PresentingApi = VulkanPresentRendering(renderer, name, device)

	override fun waitIdle() {
		device.waitIdle()
	}

	override fun delete() {

		submitter.delete()
		device.delete()

		EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, errorHandle, null)
		VK10.vkDestroyInstance(instance, null)
	}
}