package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.vulkan.*
import kotlin.math.sin

open class VulkanBufferedRenderer<E : WindowGameLogic>(override val window: WindowI, val device: VulkanDevice, surface: VulkanSurface, colourFormat: Int, colourSpace: Int) : WindowRendererI<E> {
	override val camera: CameraI = OrthographicCamera(window)

	override val viewPos: Vec3 = Vec3(0f)
	override val view: Mat4 = Mat4()
	override val projection: Mat4 = Mat4()
	override val guiProjection: Mat4 = Mat4()
	override var viewportSize: Vec2i = window.framebufferSize
	override var aspectRatio: Float = window.aspectRatio

	val commands = Array(2) { PoolAndBuffer.create(device) }
	val queue = VkUtil.createQueue(device)
	val swapchain = VkUtil.createSwapchain(device, surface, null, window.width, window.height, colourFormat, colourSpace)

	val renderFence = VkUtil.createFence(device, VK13.VK_FENCE_CREATE_SIGNALED_BIT)
	val swapchainSemaphore = VkUtil.createSemaphore(device, 0)
	val renderSemaphore = VkUtil.createSemaphore(device, 0)

	override fun init() {
		camera.init()
	}

	override fun render(game: E, tickDelta: Double) {
		renderFence.wait(1000000000L)
		renderFence.reset()
		val image = swapchain.acquireNextImage(1000000000, swapchainSemaphore.handle, null)

		val commandBeginInfo = VkCommandBufferBeginInfo.calloc()
			.`sType$Default`()
//			.pInheritanceInfo(null)
			.flags(VK13.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

		val commandBuffer = commands[0]
		commandBuffer.resetBuffer()
		commandBuffer.begin(commandBeginInfo)

		transitionImage(commandBuffer.buffer, image, VK13.VK_IMAGE_LAYOUT_UNDEFINED, VK13.VK_IMAGE_LAYOUT_GENERAL)

		val b = (sin(Timer.frameTime.toFloat()) * .5f) + .5f
		val colour = VkClearColorValue.calloc()
			.float32(0, 0f)
			.float32(1, 0f)
			.float32(2, b)
			.float32(3, 1f)
		val clearRange = VkStructs.createImageRange(VK13.VK_IMAGE_ASPECT_COLOR_BIT)

		VK13.vkCmdClearColorImage(commandBuffer.buffer, image, VK13.VK_IMAGE_LAYOUT_GENERAL, colour, clearRange)

		transitionImage(commandBuffer.buffer, image, VK13.VK_IMAGE_LAYOUT_GENERAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)

		commandBuffer.end()

		submit(commandBuffer)
		present()
	}

	fun submit(cmd: PoolAndBuffer) {
		val cmdInfo = VkStructs.createBufferSubmits(cmd.buffer, 0)
		val signalInfo = VkStructs.createSemaphoreSubmits(swapchainSemaphore.handle, KHRSynchronization2.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR, 0, 1L)
		val waitInfo = VkStructs.createSemaphoreSubmits(renderSemaphore.handle, VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT, 0, 1L)
		val submitInfo = VkStructs.createSubmitInfo2s(cmdInfo, signalInfo, waitInfo)
		VK13.vkQueueSubmit2(queue, submitInfo, renderFence.handle)
	}

	fun present() {
		val presentInfo = VkStructs.createPresentInfo(swapchain, renderSemaphore)
		KHRSwapchain.vkQueuePresentKHR(queue, presentInfo)
	}

	fun transitionImage(cmd: VkCommandBuffer, image: Long, currentLayout: Int, newLayout: Int) {

		val aspectMask = if (newLayout == VK13.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL) VK13.VK_IMAGE_ASPECT_DEPTH_BIT else VK13.VK_IMAGE_ASPECT_COLOR_BIT
		val imageBarrier = VkImageMemoryBarrier2.calloc(1)
			.`sType$Default`()
			.srcStageMask(VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
			.srcAccessMask(VK13.VK_ACCESS_2_MEMORY_WRITE_BIT)
			.dstStageMask(VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
			.dstAccessMask(VK13.VK_ACCESS_2_MEMORY_WRITE_BIT or VK13.VK_ACCESS_2_MEMORY_READ_BIT)
			.oldLayout(currentLayout)
			.newLayout(newLayout)
			.image(image)
			.subresourceRange(VkStructs.createImageRange(aspectMask))

		val dependInfo = VkDependencyInfo.calloc()
			.`sType$Default`()
			.pImageMemoryBarriers(imageBarrier)

		VK13.vkCmdPipelineBarrier2(cmd, dependInfo)
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {

		camera.updateAspectRatio(window.aspectRatio)
		viewportSize = window.size
		aspectRatio = window.aspectRatio
	}

	override fun delete() {
		swapchain.device.waitIdle()

		commands.deleteArray()
		renderFence.delete()
		swapchainSemaphore.delete()
		renderSemaphore.delete()
		swapchain.delete()
	}
}