package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.opengl.OpenGlFramebuffer
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.vulkan.VulkanManager
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
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkRenderingInfo

open class VulkanGameRenderer<in G : WindowGameLogic, C : CameraI>(override val window: WindowI, override val camera: C) : WindowRendererI<G> {

	override val viewPos: Vec3 get() = camera.cameraPos
	override val view: Mat4 = Mat4()
	override val projection: Mat4 = Mat4()
	override var guiProjection: Mat4 = Mat4()
	override var viewportSize: Vec2i = window.framebufferSize
	override var aspectRatio: Float = window.aspectRatio

	// The image that is drawn to each frame, it is then blitted onto the swapchain's current image

	val api = VulkanPresentRendering(window, VulkanManager.INSTANCE.device, ::getViewport)

	val framebuffer = getRenderingApi().createFramebuffer(window.width, window.height)

	var clearColour = Vec4(0f); private set

	override fun init() {
		(camera as Camera).range = Vec2(1000f, 0.1f)
		camera.init()
	}

	override fun render(game: G, tickDelta: Double) {
		if (window.width == 0 || window.height == 0) return


		// Vulkan uses 0-1 depth
		GLM_DEPTH_CLIP_SPACE = GlmDepthClipSpace.ZERO_TO_ONE
		camera.getView(view)
		camera.getProjection(projection)
		// Vulkan's y-axis is inverted
//		projection[1, 1] = projection[1, 1] * -1

		api.beginPresentation()
		renderWithFramebuffer(game, tickDelta)
		api.copyFramebuffer(framebuffer, this)
		api.present()
	}

	open fun renderWithFramebuffer(game: G, tickDelta: Double) {

		val api = getRenderingApi()
		api.bindFramebuffer(framebuffer)
		renderLayer(0, game, tickDelta) { transformComponent.worldPosition.z }
		renderLayer(1, game, tickDelta) { transformComponent.worldPosition.z }
		api.endFramebuffer(framebuffer)
	}

	fun beginRendering(stack: MemoryStack, cmd: PoolAndBuffer, drawImage: VulkanImage2D, depthImage: VulkanImage2D, viewport: Viewport = getViewport(), clearColour: Vec4 = this.clearColour) {
		val renderInfo = getRenderInfo(stack, drawImage, depthImage, clearColour)
		cmd.beginRendering(renderInfo)

		val api = getRenderingApi()

		api.setViewport(viewport)
		api.setScissors(viewport)
	}

	fun renderLayer(layer: Int, game: G, tickDelta: Double, framebuffer: OpenGlFramebuffer? = null) =
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

	override fun getRenderingApi(): PresentingApi {
		return api
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {

		camera.updateAspectRatio(window.aspectRatio)
		viewportSize = window.size
		aspectRatio = window.aspectRatio
		updateGui()

		framebuffer.setSize(viewportSize)
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
		api.delete()
	}
}