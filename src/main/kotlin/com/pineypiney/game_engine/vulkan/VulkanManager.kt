package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.vulkan.VulkanRendering
import com.pineypiney.game_engine.rendering.vulkan.VulkanWindowRendering
import com.pineypiney.game_engine.resources.VulkanResourceFactory
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.ApiEnum
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import kool.iterator
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

open class VulkanManager(appName: String, version: Int, layers: Collection<String>, extensions: Collection<String>) : ApiManager {

	val instance: VkInstance
	var errorHandle = 0L

	init {
		enabledLayers.addAll(layers)
	    enabledExtensions.addAll(extensions)

		instance = VkUtil.createInstance(appName, version, enabledLayers, enabledExtensions)

		// https://www.lunarg.com/wp-content/uploads/2018/05/Vulkan-Debug-Utils_05_18_v1.pdf
		if(debugEnabled){
			errorHandle = VkUtil.setupDebugger(instance, EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT, ::errorCallback)
		}
	}

	lateinit var gpu: VulkanPhysicalDevice private set
	lateinit var device: VulkanDevice private set
	lateinit var queue: VkQueue private set
	lateinit var submitter: VulkanImmediateSubmitter private set

	open val colourFormat = TextureFormat.RGBA8
	open val depthFormat = TextureFormat.DEPTH24_STENCIL8


	override fun init() {
		gpu = getPhysicalDevice()
		device = createDevice()
		queue = device.getQueue(0)
		submitter = VulkanImmediateSubmitter(this)

		device.nameObject(instance.address(), VK10.VK_OBJECT_TYPE_INSTANCE, "Vulkan Instance")
//		device.nameObject(errorHandle, EXTDebugUtils.VK_OBJECT_TYPE_DEBUG_UTILS_MESSENGER_EXT, "Error Messenger")
		device.nameObject(gpu.physicalDevice.address(), VK10.VK_OBJECT_TYPE_PHYSICAL_DEVICE, gpu.name)
		device.nameObject(device.device.address(), VK10.VK_OBJECT_TYPE_DEVICE, device.physicalDevice.name + " Vulkan Device")
	}


	open fun getPhysicalDevice(): VulkanPhysicalDevice{
		val device = VulkanPhysicalDevice(instance, VkUtil.getPhysicalDevices(instance).first())
			.addFeature(VkPhysicalDeviceBufferDeviceAddressFeatures.calloc().`sType$Default`().bufferDeviceAddress(true), VkDeviceCreateInfo::pNext)
			.addFeature(VkPhysicalDeviceSynchronization2Features.calloc().`sType$Default`().synchronization2(true), VkDeviceCreateInfo::pNext)
			.addFeature(VkPhysicalDeviceDynamicRenderingFeatures.calloc().`sType$Default`().dynamicRendering(true), VkDeviceCreateInfo::pNext)
		return device
	}

	open fun createDevice(): VulkanDevice{
		val extensions = mutableListOf(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
		addExtensions(instance.capabilities.apiVersion, extensions)
		return gpu.createDevice(extensions)
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

	override val getEnum: (ApiEnum) -> Int = ApiEnum::vulkan

	override fun invertY(): Boolean  = true

	override fun createResourceFactory(): VulkanResourceFactory = VulkanResourceFactory(this)

	override fun createRenderingApi(name: String): VulkanRendering = VulkanRendering.create(device, name)

	override fun createPresentingApi(window: WindowI, name: String): VulkanWindowRendering = VulkanWindowRendering(window, name, device)

	override fun waitIdle() {
		device.waitIdle()
	}


	override fun getMaxTextureSide1D(): Int = device.physicalDevice.getMaxTextureSide1D()
	override fun getMaxTextureSide2D(): Int = device.physicalDevice.getMaxTextureSide2D()
	override fun getMaxTextureSide3D(): Int = device.physicalDevice.getMaxTextureSide3D()
	override fun getMaxVertexAttribs(): Int = device.physicalDevice.getVertexAttribs()
	override fun getMaxViewportDimensions(): Vec2i = device.physicalDevice.getMaxViewportDimensions()

	override fun getMaxTesselationPatchVertices(): Int = device.physicalDevice.getMaxTesselationPatchVertices()
	override fun getMaxTesselationLevel(): Int = device.physicalDevice.getMaxTesselationLevel()
	override fun getMaxComputeWorkGroupSize(): Vec3i = device.physicalDevice.getMaxComputeWorkGroupSize()
	override fun getMaxComputeWorkGroupCount(): Vec3i = device.physicalDevice.getMaxComputeWorkGroupCount()

	override fun delete() {

		submitter.delete()
		device.delete()

		gpu.free()
		if(errorHandle != 0L) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, errorHandle, null)
		VK10.vkDestroyInstance(instance, null)
	}

	companion object {
		val enabledLayers = mutableListOf<String>()
		val enabledExtensions = mutableListOf<String>()
		val debugEnabled get() = enabledExtensions.contains(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)

		fun validated(appName: String, version: Int, glfw: Boolean): VulkanManager {
			val layers = listOf("VK_LAYER_KHRONOS_validation")
			val exts = mutableListOf(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)

			if(glfw) {
				val glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions() ?: throw RuntimeException("Missing required GLFW extensions")
				for(extension in glfwExtensions) enabledExtensions.add(MemoryUtil.memUTF8(extension))
			}

			return VulkanManager(appName, version, layers, exts)
		}

		fun addExtensions(version: Int, extensions: MutableList<String>) {
			if(version < VK13.VK_API_VERSION_1_3) {
				extensions.add(KHRDynamicRendering.VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME)
				if(version < VK13.VK_API_VERSION_1_2) {
					extensions.addAll(
						KHRBufferDeviceAddress.VK_KHR_BUFFER_DEVICE_ADDRESS_EXTENSION_NAME,
						KHRDepthStencilResolve.VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME,
						KHRCreateRenderpass2.VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME,
					)
					if(version < VK13.VK_API_VERSION_1_1) {
						extensions.addAll(
							KHRMultiview.VK_KHR_MULTIVIEW_EXTENSION_NAME,
							KHRGetPhysicalDeviceProperties2.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME,
							KHRMaintenance2.VK_KHR_MAINTENANCE_2_EXTENSION_NAME,
						)
					}
				}
			}
		}
	}
}