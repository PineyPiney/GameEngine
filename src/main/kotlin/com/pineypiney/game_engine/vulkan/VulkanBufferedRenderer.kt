package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

open class VulkanBufferedRenderer<E : WindowGameLogic>(override val window: WindowI, val vulkan: VulkanManager) : WindowRendererI<E> {
	override val camera: CameraI = OrthographicCamera(window)

	override val viewPos: Vec3 = Vec3(0f)
	override val view: Mat4 = Mat4()
	override val projection: Mat4 = Mat4()
	override val guiProjection: Mat4 = Mat4()
	override var viewportSize: Vec2i = window.framebufferSize
	override var aspectRatio: Float = window.aspectRatio

	val commands get() = vulkan.commands
	val swapchain get() = vulkan.swapchain
	val drawImage get() = vulkan.drawImage


	val pipeline = ShaderLoader.generateComputePipelineVulkan(vulkan, ShaderLoader.INSTANCE.shaderModules[ResourceKey("compute/mouse_pos_vulkan")]!!, 8)

	override fun init() {
		camera.init()
		updateDescriptorSets()
	}

	override fun render(game: E, tickDelta: Double) {
		// Wait until the fence is ready, it will be signalled by the previous render cycle
		vulkan.renderFence.wait(1000000000L)

		// Get the next swapchain image to draw to, and signal the swapchain semaphore once fetched
		val swapchainImage = swapchain.acquireNextImage(1000000000, vulkan.swapchainSemaphore.handle, null)
		vulkan.renderFence.reset()

		val commandBeginInfo = VkCommandBufferBeginInfo.calloc()
			.`sType$Default`()
			.flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

		val commandBuffer = commands[swapchain.imageIndex.coerceIn(commands.indices)]
		commandBuffer.resetBuffer()
		commandBuffer.begin(commandBeginInfo)

//		renderToSwapchain(commandBuffer, swapchainImage)
		renderWithFramebuffer(commandBuffer, swapchainImage)
//		val buffer = swapchainImage.getData(4)
//		buffer.free()

		commandBuffer.end()

		submit(commandBuffer)
		present()
	}

	fun renderWithFramebuffer(commandBuffer: PoolAndBuffer, swapchainImage: VulkanSwapchainImage) {

		// Set the Draw Image's mode to general
		drawImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_GENERAL, false)

		commandBuffer.bindPipeline(pipeline)
		commandBuffer.bindDescriptorSets(vulkan, pipeline)
		val mousePos = window.input.mouse.lastPos.pixels
		val constants = MemoryUtil.memAlloc(8).putInt(mousePos.x).putInt(window.height - mousePos.y).flip()
		commandBuffer.pushConstants(pipeline, constants)

		commandBuffer.dispatch(Vec3i(Math.ceilDiv(swapchainImage.size.x, 16), Math.ceilDiv(swapchainImage.size.y, 16), 1))


		// Copy the Draw Image to the Swapchain Image
		drawImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		swapchainImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, false)
		drawImage.copyTo(commandBuffer.buffer, swapchainImage, drawImage.size, window.framebufferSize)

		// Prepare the Swapchain Image for presentation
		swapchainImage.transition(commandBuffer.buffer, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
	}

	fun submit(cmd: PoolAndBuffer) {
		val cmdInfo = VkStructs.createBufferSubmits(cmd.buffer, 0)
		// Wait for the swapchain semaphore
		val waitInfo = VkStructs.createSemaphoreSubmits(vulkan.swapchainSemaphore.handle, KHRSynchronization2.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR, 0, 1L)
		// Signal the render semaphore
		val signalInfo = VkStructs.createSemaphoreSubmits(vulkan.renderSemaphore.handle, VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT, 0, 1L)
		val submitInfo = VkStructs.createSubmitInfo2s(cmdInfo, signalInfo, waitInfo)
		VK13.vkQueueSubmit2(vulkan.queue, submitInfo, vulkan.renderFence.handle)
	}

	fun present() {
		// Wait for the render semaphore, and then present the swapchain to the screen
		val presentInfo = VkStructs.createPresentInfo(swapchain, vulkan.renderSemaphore)
		KHRSwapchain.vkQueuePresentKHR(vulkan.queue, presentInfo)
	}

	fun updateDescriptorSets() {
		val imageInfo = VkDescriptorImageInfo.calloc(1)
			.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL)
			.imageView(drawImage.imageView)

		val writeSet = VkWriteDescriptorSet.calloc(1)
			.`sType$Default`()
			.dstBinding(0)
			.dstSet(vulkan.descriptorSet)
			.descriptorCount(1)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
			.pImageInfo(imageInfo)

		VK10.vkUpdateDescriptorSets(vulkan.device.device, writeSet, null)
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {

		camera.updateAspectRatio(window.aspectRatio)
		viewportSize = window.size
		aspectRatio = window.aspectRatio
	}

	override fun delete() {
	}
}