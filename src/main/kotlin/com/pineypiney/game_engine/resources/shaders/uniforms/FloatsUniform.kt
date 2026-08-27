package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

class FloatsUniform(
	name: String,
	default: FloatBuffer = BufferUtils.createFloatBuffer(0),
	getter: UniformGetter<FloatBuffer> = { BufferUtils.createFloatBuffer(0) }
) : Uniform<FloatBuffer>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setFloats(name, getValue(renderer))
	}
}