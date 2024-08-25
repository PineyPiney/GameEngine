package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.video.Video
import glm_.vec2.Vec2

abstract class VideoRendererComponent(parent: GameObject) :
	ShaderRenderedComponent(parent, MeshedTextureComponent.default2DShader) {

	abstract val video: Video
	val vShape = Companion.shape

	override fun render(renderer: RendererI, tickDelta: Double) {

		val tex = video.getCurrentTexture()
		tex.bind()

		shader.setUp(uniforms, renderer)

		vShape.bindAndDraw()
	}

	fun play() = video.play()
	fun pause() = video.pause()
	fun resume() = video.resume()
	fun stop() = video.stop()

	override fun delete() {
		super.delete()
		video.delete()
	}

	companion object {
		// Image must be flipped vertically
		val shape = SquareShape(Vec2(0f), Vec2(1f), Vec2(0f, 1f), Vec2(1f, 0f))
	}
}