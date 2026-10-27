package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorAllocator
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import com.pineypiney.game_engine.vulkan.*
import com.pineypiney.game_engine.window.Window
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkQueue

class VulkanWindowRendering(val window: Window, val name: String, device: VulkanDevice) : VulkanRendering(), PresentingApi {

	val surface = VkUtil.createSurface(device.instance, window)
	val colourFormatSpace = device.physicalDevice.getSurfaceColour(surface)
	var swapchain = VkUtil.createSwapchain("$name Swapchain", device, surface, null, window.width, window.height, colourFormatSpace.first, colourFormatSpace.second)

	init {
		device.nameObject(surface.handle, KHRSurface.VK_OBJECT_TYPE_SURFACE_KHR, "$name Surface")
	}

	var frameIndex = 0
	val frames: Array<VulkanFrameObjects> = Array(swapchain.images.size) { VulkanFrameObjects(device, name) }
	val frameObjects get() = frames[frameIndex]

	override val cmd: PoolAndBuffer get() = frameObjects.commands
	override val descriptorAllocator: VulkanDescriptorAllocator get() = frameObjects.frameDescriptorAllocator

	override fun beginPresentation(): Boolean {
		if (window.minimised) return false
		// Clear old frame data
		frameObjects.refresh()
		// Wait until the fence is ready, it will be signalled by the previous render cycle
		frameObjects.renderFence.wait(1000000000L)

		// Get the next swapchain image to draw to, and signal the swapchain semaphore once fetched
		val swapchainImage = swapchain.acquireNextImage(1000000000, frameObjects.swapchainSemaphore, null)
		if (swapchainImage == null) {
			updateSwapchain(window.framebufferSize)
			return false
		}

		frameObjects.renderFence.reset()
		frameObjects.begin()
		return true
	}

	override fun copyFramebuffer(framebuffer: Framebuffer, window: Window, renderer: RendererI) {

		// Copy the Draw Image to the Swapchain Image
		val drawImage = framebuffer.colour as VulkanImage2D
		val swapchainImage = swapchain.currentImage()
		drawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		swapchainImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, false)
		drawImage.copyTo(cmd, swapchainImage, Vec3i(0, drawImage.height, 0), Vec3i(drawImage.width, 0, 1))

		// Prepare the Swapchain Image for presentation
		swapchainImage.transition(cmd, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
	}

	override fun present() {
		cmd.end()
		val queue = device.getQueue(0)
		submit(queue)
		present(queue)
		VK10.vkQueueWaitIdle(queue)
		frameIndex = (frameIndex + 1) % frames.size
	}


	fun submit(queue: VkQueue) {
		MemoryStack.stackPush().use { stack ->
			VkUtil.submitQueue(stack, queue, cmd, frameObjects.renderFence, frameObjects.swapchainSemaphore, frameObjects.renderSemaphore)
		}
	}

	fun present(queue: VkQueue) {
		MemoryStack.stackPush().use { stack ->
			// Wait for the render semaphore, and then present the swapchain to the screen
			val presentInfo = VkStructs.createPresentInfo(stack, swapchain, frameObjects.renderSemaphore)
			val err = KHRSwapchain.vkQueuePresentKHR(queue, presentInfo)
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				if (!window.minimised) {
					VK10.vkQueueWaitIdle(queue)
					updateSwapchain(window.framebufferSize)
				}
			} else VkUtil.processResult(err, "Failed to present swapchain image to screen")
		}
	}

	fun updateSwapchain(size: Vec2i) {
		device.waitIdle()

		swapchain = VkUtil.createSwapchain("$name Swapchain", device, surface, swapchain, size.x, size.y, colourFormatSpace.first, colourFormatSpace.second)
	}

	override fun delete() {
		frames.deleteArray()
		swapchain.delete()
		surface.delete()
	}
}