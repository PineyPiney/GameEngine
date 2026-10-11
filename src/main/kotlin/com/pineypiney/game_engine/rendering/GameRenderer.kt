package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4

abstract class GameRenderer<in E : GameLogicI>(override val window: WindowI, apiFact: (GameRenderer<E>) -> PresentingApi) : WindowRendererI<E> {

	private val api = apiFact(this)
	open val framebuffer = api.createFramebuffer(window.width, window.height)

	override val viewPos: Vec3 get() = camera.cameraPos
	override var aspectRatio: Float = 1f
	override var viewportSize: Vec2i = window.framebufferSize

	override val view = I
	override val projection = I
	override val guiProjection = I

	override fun init() {
		camera.init()
		viewportSize = window.framebufferSize
		framebuffer.init()
	}

	override fun getRenderingApi(): PresentingApi = api


	fun renderLayer(layer: Int, game: E, tickDelta: Double) = renderLayer(game.gameObjects[layer], tickDelta) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	fun <C : Comparable<C>> renderLayer(layer: Int, game: E, tickDelta: Double, sort: GameObject.() -> C) = renderLayer(game.gameObjects[layer], tickDelta, sort)

	fun renderLayer(layer: Collection<GameObject>, tickDelta: Double) = renderLayer(layer, tickDelta) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	open fun <C : Comparable<C>> renderLayer(layer: Collection<GameObject>, tickDelta: Double, sort: GameObject.() -> C) {
		val sortedObjects = layer.flatMap { it.catchRenderingComponents() }.sortedBy(sort)
		for (o in sortedObjects) {
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

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		camera.updateAspectRatio(window.aspectRatio)
		framebuffer.setSize(window.framebufferSize)
		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio
		getRenderingApi().updateGui(guiProjection, aspectRatio)
	}

	override fun setClearColour(colour: Vec4) {
//		GLFunc.clearColour = colour
	}

	open fun deleteFrameBuffers() {
		framebuffer.delete()
	}

	override fun delete() {
		deleteFrameBuffers()
		api.delete()
	}

	companion object {
		val screenShader = ShaderLoader.loadRenderShader(ResourceKey("menu/screen"))
		val screenUniforms = screenShader.compileUniforms()
	}
}