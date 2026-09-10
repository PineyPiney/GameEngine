package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.opengl.Framebuffer
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import com.pineypiney.game_engine.vulkan.*
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.detail.GLM_DEPTH_CLIP_SPACE
import glm_.detail.GlmDepthClipSpace
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*

open class VulkanGameRenderer<in G : WindowGameLogic, C : CameraI>(override val window: WindowI, val vulkan: VulkanManager, override val camera: C) : WindowRendererI<G> {

	override val viewPos: Vec3 get() = camera.cameraPos
	override val view: Mat4 = Mat4()
	override val projection: Mat4 = Mat4()
	override var guiProjection: Mat4 = Mat4()
	override var viewportSize: Vec2i = window.framebufferSize
	override var aspectRatio: Float = window.aspectRatio

	val surface = VkUtil.createSurface(vulkan.instance, window)
	val colourFormatSpace = vulkan.gpu.getSurfaceColour(surface)
	var swapchain = VkUtil.createSwapchain(vulkan.device, surface, null, window.width, window.height, colourFormatSpace.first, colourFormatSpace.second)

	// The image that is drawn to each frame, it is then blitted onto the swapchain's current image
	lateinit var drawImage: VulkanImage2D
	lateinit var depthImage: VulkanImage2D

	var frameIndex = 0
	val frameObjects = Array(swapchain.images.size) { VulkanFrameObjects(vulkan.device, ::getViewport) }

	var clearColour = Vec4(0f); private set

	override fun init() {
		(camera as Camera).range = Vec2(1000f, 0.1f)
		camera.init()
		updateFrameImages()
	}

	override fun render(game: G, tickDelta: Double) {
		if (window.width == 0 || window.height == 0) return


		// Vulkan uses 0-1 depth
		GLM_DEPTH_CLIP_SPACE = GlmDepthClipSpace.ZERO_TO_ONE
		camera.getView(view)
		camera.getProjection(projection)
		// Vulkan's y-axis is inverted
//		projection[1, 1] = projection[1, 1] * -1

		val frameObjects = frameObjects[frameIndex]
		// Clear old frame data
		frameObjects.refresh()
		// Wait until the fence is ready, it will be signalled by the previous render cycle
		frameObjects.renderFence.wait(1000000000L)

		// Get the next swapchain image to draw to, and signal the swapchain semaphore once fetched
		val swapchainImage = swapchain.acquireNextImage(1000000000, frameObjects.swapchainSemaphore, null)
		if (swapchainImage == null) {
			updateSwapchain(window.size)
			return
		}

		frameObjects.renderFence.reset()

		val cmd = frameObjects.begin()
		renderWithFramebuffer(cmd, game, tickDelta, swapchainImage)
		cmd.end()

		end()
	}

	open fun renderWithFramebuffer(cmd: PoolAndBuffer, game: G, tickDelta: Double, swapchainImage: VulkanSwapchainImage) {

		// Execute Graphics Shader
		drawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
		depthImage.transition(cmd, VK12.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, false)

		MemoryStack.stackPush().use { stack ->
			beginRendering(stack, cmd, drawImage, depthImage)
			renderLayer(0, game, tickDelta) { transformComponent.worldPosition.z }
			renderLayer(1, game, tickDelta) { transformComponent.worldPosition.z }
			cmd.endRendering()
		}

		// Copy the Draw Image to the Swapchain Image
		drawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		swapchainImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, false)
		drawImage.copyTo(cmd, swapchainImage, Vec3i(0, drawImage.height, 0), Vec3i(drawImage.width, 0, 1))

		// Prepare the Swapchain Image for presentation
		swapchainImage.transition(cmd, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
	}

	fun beginRendering(stack: MemoryStack, cmd: PoolAndBuffer, drawImage: VulkanImage2D, depthImage: VulkanImage2D, viewport: Viewport = getViewport(), clearColour: Vec4 = this.clearColour) {
		val renderInfo = getRenderInfo(stack, drawImage, depthImage, clearColour)
		cmd.beginRendering(renderInfo)

		val api = getRenderingApi()

		api.setViewport(viewport)
		api.setScissors(viewport)
	}

	fun renderLayer(layer: Int, game: G, tickDelta: Double, framebuffer: Framebuffer? = null) =
		renderLayer(game.gameObjects[layer], tickDelta) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	fun <C : Comparable<C>> renderLayer(layer: Int, game: G, tickDelta: Double, sort: GameObject.() -> C) =
		renderLayer(game.gameObjects[layer], tickDelta, sort)

	fun renderLayer(layer: Collection<GameObject>, tickDelta: Double) =
		renderLayer(layer, tickDelta) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	open fun <C : Comparable<C>> renderLayer(layer: Collection<GameObject>, tickDelta: Double, sort: GameObject.() -> C) {
		val sorted = layer.flatMap { it.catchRenderingComponents() }.sortedBy(sort)
		for (o in sorted) {
			renderObject(o, tickDelta)
		}
	}

	open fun renderObject(obj: GameObject, tickDelta: Double) {
		val renderedComponents = obj.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
		if (renderedComponents.isNotEmpty()) {
			for (c in obj.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
			for (c in renderedComponents) c.render(this, tickDelta)
		} else for (c in obj.components.filterIsInstance<PreRenderComponent>()) {
			if (!c.whenVisible) c.preRender(this, tickDelta)
		}
	}

	fun submit() {
		MemoryStack.stackPush().use { stack ->
			val frameObjects = frameObjects[frameIndex]
			val cmdInfo = VkStructs.createBufferSubmits(stack, frameObjects.commands.buffer, 0)
			// Wait for the swapchain semaphore
			val waitInfo = VkStructs.createSemaphoreSubmits(stack, frameObjects.swapchainSemaphore, KHRSynchronization2.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR, 0, 1L)
			// Signal the render semaphore
			val signalInfo = VkStructs.createSemaphoreSubmits(stack, frameObjects.renderSemaphore, VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT, 0, 1L)
			val submitInfo = VkStructs.createSubmitInfo2s(stack, cmdInfo, signalInfo, waitInfo)
			VK13.vkQueueSubmit2(vulkan.queue, submitInfo, frameObjects.renderFence.handle)
		}
	}

	fun present() {
		MemoryStack.stackPush().use { stack ->
			// Wait for the render semaphore, and then present the swapchain to the screen
			val presentInfo = VkStructs.createPresentInfo(stack, swapchain, frameObjects[frameIndex].renderSemaphore)
			val err = KHRSwapchain.vkQueuePresentKHR(vulkan.queue, presentInfo)
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				updateSwapchain(window.size)
			} else VkUtil.processResult(err, "Failed to present swapchain image to screen")
		}
	}

	fun end() {
		submit()
		present()
		VK10.vkQueueWaitIdle(vulkan.queue)
		frameIndex = (frameIndex + 1) % this.frameObjects.size
	}

	fun updateSwapchain(size: Vec2i) {
		vulkan.device.waitIdle()
		swapchain = VkUtil.createSwapchain(vulkan.device, surface, swapchain, size.x, size.y, colourFormatSpace.first, colourFormatSpace.second)
	}

	open fun updateFrameImages() {
		val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
				VK10.VK_IMAGE_USAGE_STORAGE_BIT or
				VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT

		drawImage = VkUtil.createImage(vulkan.device, "Draw Image", vulkan.drawFormat, usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, Vec2i(window.size))
		depthImage = VkUtil.createImage(
			vulkan.device,
			"Depth Image",
			vulkan.depthFormat,
			VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
			VK10.VK_IMAGE_ASPECT_DEPTH_BIT or VK10.VK_IMAGE_ASPECT_STENCIL_BIT,
			Vec2i(window.size)
		)
	}

	override fun getRenderingApi(): RenderingApi {
		return frameObjects[frameIndex].api
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {

		camera.updateAspectRatio(window.aspectRatio)
		viewportSize = window.size
		aspectRatio = window.aspectRatio

		updateGui()

		drawImage.delete()
		depthImage.delete()
		updateFrameImages()

//		for(objects in frameObjects){
//			objects.swapchainSemaphore.recreate()
//			objects.renderSemaphore.recreate()
//		}
	}

	fun updateGui() {
		guiProjection = glm.ortho(-aspectRatio, aspectRatio, -1f, 1f)
		guiProjection.translateAssign(0f, 0f, -1f)
	}

	override fun setClearColour(colour: Vec4) {
		clearColour = colour
	}

	fun getRenderInfo(stack: MemoryStack, drawImage: VulkanImage2D, depthImage: VulkanImage2D, clearColour: Vec4 = this.clearColour): VkRenderingInfo {
		val colourAttachments = VkStructs.createColourAttachmentInfos(stack, drawImage, clearColour)
		val depthAttachment = VkStructs.createDepthStencilAttachmentInfo(stack, depthImage, 0f, 0)
		return VkStructs.createRenderingInfo(stack, glm.min(window.size, drawImage.size), colourAttachments, depthAttachment)
	}

	override fun delete() {
		frameObjects.deleteArray()
		drawImage.delete()
		depthImage.delete()
		swapchain.delete()
		surface.delete()
	}
}