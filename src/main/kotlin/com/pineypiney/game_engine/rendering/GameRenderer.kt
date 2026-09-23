package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.ObjectCollection
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

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		camera.updateAspectRatio(window.aspectRatio)
		framebuffer.setSize(window.framebufferSize)
		viewportSize = window.size
		aspectRatio = window.aspectRatio
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
		val screenShader = ShaderLoader[ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer")]
		val screenUniforms = screenShader.compileUniforms()
	}
}