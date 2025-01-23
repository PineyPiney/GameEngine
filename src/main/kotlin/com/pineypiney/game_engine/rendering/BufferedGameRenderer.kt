package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

abstract class BufferedGameRenderer<E : GameLogicI> : WindowRendererI<E> {

	val buffer = FrameBuffer(0, 0)

	override val viewPos: Vec3 get() = camera.cameraPos
	override lateinit var viewportSize: Vec2i
	override var aspectRatio: Float = 1f

	override val numPointLights: Int = 4

	override fun init() {
		camera.init()
		buffer.setSize(window.framebufferSize)
		viewportSize = window.framebufferSize
	}

	open fun clearFrameBuffer(buffer: FrameBuffer = this.buffer) {
		buffer.bind()
		viewportSize = Vec2i(buffer.width, buffer.height)
		GLFunc.viewportO = viewportSize
		clear()
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		camera.updateAspectRatio(window.aspectRatio)
		buffer.setSize(window.framebufferSize)
		viewportSize = window.size
		aspectRatio = window.aspectRatio
	}

	open fun deleteFrameBuffers() {
		buffer.delete()
	}

	override fun delete() {
		deleteFrameBuffers()
	}

	companion object {
		val screenShader =
			ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
		val screenUniforms = screenShader.compileUniforms()
	}
}