package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.rendering.openxr.OpenXrFramebuffer
import com.pineypiney.game_engine.rendering.openxr.OpenXrVkFramebuffer
import com.pineypiney.game_engine.rendering.openxr.OpenXrVulkanManager
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.util.LwjglUtil
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.xr.XrUtil.getVulkanSwapchainImages
import glm_.vec3.Vec3i
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10

object VulkanXr : XrGraphicsApi {

	override fun getName(): String = "Vulkan"
	override fun getExtensions(): List<String> = listOf(KHRVulkanEnable.XR_KHR_VULKAN_ENABLE_EXTENSION_NAME, KHRVulkanEnable2.XR_KHR_VULKAN_ENABLE2_EXTENSION_NAME)

	override fun getRequirements(stack: MemoryStack, instance: XrInstance, systemID: Long): Pair<Vec3i, Vec3i> {
		val requirements = XrGraphicsRequirementsVulkan2KHR.calloc(stack).`type$Default`()
		KHRVulkanEnable2.xrGetVulkanGraphicsRequirements2KHR(instance, systemID, requirements)
		return LwjglUtil.version(requirements.minApiVersionSupported()) to LwjglUtil.version(requirements.maxApiVersionSupported())
	}

	override fun getColourFormats(): List<TextureFormat> = listOf(TextureFormat.SRGBA8, TextureFormat.SBGRA8, TextureFormat.RGBA8, TextureFormat.BGRA8)

	fun getApiExtensions(xrInstance: XrInstance, systemId: Long): List<String>{
		MemoryStack.stackPush().use { stack ->
			val buffer = LwjglUtil.enumerate(stack, "OpenXR Vulkan Extensions", xrInstance, systemId, MemoryStack::malloc, KHRVulkanEnable::xrGetVulkanInstanceExtensionsKHR) ?: return emptyList()
			val string = MemoryUtil.memUTF8(buffer, buffer.remaining() - 1)
			return string.split(" ")
		}
	}

	fun getDeviceExtensions(xrInstance: XrInstance, systemId: Long): List<String>{
		MemoryStack.stackPush().use { stack ->
			val buffer = LwjglUtil.enumerate(stack, "OpenXR Vulkan Device Extensions", xrInstance, systemId, MemoryStack::malloc, KHRVulkanEnable::xrGetVulkanDeviceExtensionsKHR) ?: return emptyList()
			val string = MemoryUtil.memUTF8(buffer, buffer.remaining() - 1)
			return string.split(" ")
		}
	}

	override fun createApi(name: String, xrInstance: XrInstance, systemId: Long): ApiManager =
		OpenXrVulkanManager(name, xrInstance, systemId)

	fun createSwapchainImage(stack: MemoryStack, device: VulkanDevice, width: Int, height: Int, i: Int, image: Long, format: Int, aspect: Int): VulkanSwapchainImage{
		val format = TextureFormat.fromVulkanConst(format) ?: TextureFormat.RGBA8
		val view = VkUtil.createImageView(stack, device, VK10.VK_IMAGE_TYPE_2D, image, format, aspect)
		device.nameObject(image, VK10.VK_OBJECT_TYPE_IMAGE, "OpenXR Swapchain Image $i")
		device.nameObject(view, VK10.VK_OBJECT_TYPE_IMAGE_VIEW, "OpenXR Swapchain Image View $i")
		return VulkanSwapchainImage(device, image, view, format, width, height)
	}

	override fun createFramebuffer(
		stack: MemoryStack,
		api: ApiManager,
		width: Int,
		height: Int,
		colourSwapchain: XrSwapchain,
		depthSwapchain: XrSwapchain,
		colourFormat: Int,
		depthFormat: Int
	): OpenXrFramebuffer {
		val device = (api as VulkanManager).device

		val colourImages = getVulkanSwapchainImages(stack, colourSwapchain)
		val depthImages = getVulkanSwapchainImages(stack, depthSwapchain)

		return OpenXrVkFramebuffer(
			device,
			colourImages.mapIndexed { i, image ->
				createSwapchainImage(
					stack,
					device,
					width,
					height,
					i,
					image.image(),
					colourFormat,
					VK10.VK_IMAGE_ASPECT_COLOR_BIT
				)
			},
			depthImages.mapIndexed { i, image ->
				createSwapchainImage(
					stack,
					device,
					width,
					height,
					i,
					image.image(),
					depthFormat,
					VK10.VK_IMAGE_ASPECT_DEPTH_BIT
				)
			}
		)
	}

	override fun createSession(stack: MemoryStack, openxr: OpenXrManager, graphicsApi: ApiManager, window: WindowI): OpenXrSession {
		return XrUtil.createVulkanSession(stack, openxr, graphicsApi as VulkanManager)
	}
}