package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.video.Video
import glm_.vec2.Vec2

abstract class VideoRendererComponent(parent: GameObject) :
	ShaderRenderedComponent(parent, MeshedTextureComponent.default2DShader) {

	abstract val video: Video
	val mesh = shape

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setTextureUniform("tex", video::getCurrentTexture)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		shader.draw("vertexBuffer", mesh, renderer)
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
		val shape = Mesh.textureQuad(ResourceFactory.INSTANCE, "Video Quad", Vec2(0f), Vec2(1f), Vec2(0f, 1f), Vec2(1f, 0f))
	}
}