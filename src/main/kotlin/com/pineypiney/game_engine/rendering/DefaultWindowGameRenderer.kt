package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.vec4.Vec4

open class DefaultWindowGameRenderer<in G : WindowGameLogic, C : CameraI>(window: WindowI, override val camera: C, api: (GameRenderer<G>) -> PresentingApi) : GameRenderer<G>(window, api) {

	constructor(window: WindowI, camera: (WindowI) -> C, api: (GameRenderer<G>) -> PresentingApi) : this(window, camera(window), api)

	override fun init() {
		super.init()
		screenUniforms.setIntUniform("effects") { 0 }
	}

	override fun render(game: G, tickDelta: Double) {

		camera.getView(view)
		camera.getProjection(projection)

		val api = getRenderingApi()
		api.beginPresentation()
		api.bindFramebuffer(framebuffer, Vec4(1f, 0f, 0f, 1f))

		renderLayer(0, game, tickDelta)
		renderLayer(1, game, tickDelta) { transformComponent.worldPosition.z }

		api.endFramebuffer(framebuffer)
		// This draws the buffer onto the screen
		api.copyFramebuffer(framebuffer, this)
		api.present()
	}

	fun renderLayer(layer: Int, game: G, tickDelta: Double) = renderLayer(game.gameObjects[layer], tickDelta) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	fun <C : Comparable<C>> renderLayer(layer: Int, game: G, tickDelta: Double, sort: GameObject.() -> C) = renderLayer(game.gameObjects[layer], tickDelta, sort)

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
		super.updateAspectRatio(window, objects)
		getRenderingApi().updateGui(guiProjection, window.aspectRatio)
	}
}