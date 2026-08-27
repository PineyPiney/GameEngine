package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4

abstract class OpenGlGameRenderer<in E : GameLogicI> : WindowRendererI<E> {

	val framebuffer = Framebuffer(0, 0)

	override val viewPos: Vec3 get() = camera.cameraPos
	override lateinit var viewportSize: Vec2i
	override var aspectRatio: Float = 1f
	private val renderingApi: RenderingApi = OpenGlRendering

	override fun init() {
		camera.init()
		framebuffer.setSize(window.framebufferSize)
		viewportSize = window.framebufferSize
	}

	open fun clearFrameBuffer(buffer: Framebuffer = this.framebuffer) {
		buffer.bind()
		viewportSize = Vec2i(buffer.width, buffer.height)
		GLFunc.viewportO = viewportSize
		clear()
	}

	override fun getRenderingApi(): RenderingApi = renderingApi

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		camera.updateAspectRatio(window.aspectRatio)
		framebuffer.setSize(window.framebufferSize)
		viewportSize = window.size
		aspectRatio = window.aspectRatio
	}

	override fun setClearColour(colour: Vec4) {
		GLFunc.clearColour = colour
	}

	open fun deleteFrameBuffers() {
		framebuffer.delete()
	}

	override fun delete() {
		deleteFrameBuffers()
	}

	companion object {
		val screenShader =
			ShaderLoader.get(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
		val screenUniforms = screenShader.compileUniforms()
	}
}