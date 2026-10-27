package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.vulkan.VulkanPhysicalDevice
import com.pineypiney.game_engine.window.WindowApi
import com.pineypiney.game_engine.xr.VulkanXr
import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.KHRVulkanEnable2
import org.lwjgl.openxr.XrInstance
import org.lwjgl.openxr.XrVulkanGraphicsDeviceGetInfoKHR
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*

class OpenXrVulkanManager(appName: String, val xrInstance: XrInstance, val systemId: Long, windowApi: WindowApi) : VulkanManager(appName, TARGET_VERSION, listOf("VK_LAYER_KHRONOS_validation"), createInstanceExtensions(xrInstance, systemId, windowApi)) {

	override var colourFormat: TextureFormat = super.colourFormat
	override var depthFormat: TextureFormat = super.depthFormat

	override fun getPhysicalDevice(): VulkanPhysicalDevice {
		MemoryStack.stackPush().use { stack ->
			val all = VkUtil.getPhysicalDevices(instance)

			val b = stack.mallocPointer(1)
			val getInfo = XrVulkanGraphicsDeviceGetInfoKHR.calloc(stack).`type$Default`()
				.systemId(systemId)
				.vulkanInstance(instance)
			XrUtil.processResult(KHRVulkanEnable2.xrGetVulkanGraphicsDevice2KHR(xrInstance, getInfo, b), "Failed to get Vulkan Graphics Device for OpenXR")

			if(!all.contains(b[0])) b.put(0, all.first())

			val gpu = VulkanPhysicalDevice(instance, b[0])
				.addFeature(VkPhysicalDeviceBufferDeviceAddressFeatures.calloc().`sType$Default`().bufferDeviceAddress(true), VkDeviceCreateInfo::pNext)
				.addFeature(VkPhysicalDeviceSynchronization2Features.calloc().`sType$Default`().synchronization2(true), VkDeviceCreateInfo::pNext)
				.addFeature(VkPhysicalDeviceDynamicRenderingFeatures.calloc().`sType$Default`().dynamicRendering(true), VkDeviceCreateInfo::pNext)
				.addFeature(VkPhysicalDeviceTimelineSemaphoreFeatures.calloc().`sType$Default`().timelineSemaphore(true), VkDeviceCreateInfo::pNext)

			return gpu
		}
	}

	override fun createDevice(): VulkanDevice {
		val xrExts = VulkanXr.getDeviceExtensions(xrInstance, systemId).toMutableList()
		xrExts.add(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
		addExtensions(instance.capabilities.apiVersion, xrExts)
		if(instance.capabilities.apiVersion < VK12.VK_API_VERSION_1_2){
			xrExts.add(KHRSeparateDepthStencilLayouts.VK_KHR_SEPARATE_DEPTH_STENCIL_LAYOUTS_EXTENSION_NAME)
		}
		return gpu.createDevice(xrExts)
	}

	companion object {
		val TARGET_VERSION = VK12.VK_API_VERSION_1_1

		fun createInstanceExtensions(xrInstance: XrInstance, systemId: Long, windowApi: WindowApi): List<String>{
			val extensions = VulkanXr.getApiExtensions(xrInstance, systemId).toMutableList()
			extensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
			extensions.addAll(windowApi.getVulkanExtensions())
			return extensions
		}
	}
}