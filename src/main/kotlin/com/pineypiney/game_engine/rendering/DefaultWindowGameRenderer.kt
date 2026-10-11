package com.pineypiney.game_engine.rendering

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
		if (!api.beginPresentation()) return
		api.bindFramebuffer(framebuffer, Vec4(1f, 0f, 0f, 1f))

		renderLayer(0, game, tickDelta)
		renderLayer(1, game, tickDelta) { transformComponent.worldPosition.z }

		api.endFramebuffer(framebuffer)
		// This draws the buffer onto the screen
		api.copyFramebuffer(framebuffer, this)
		api.present()
	}
}