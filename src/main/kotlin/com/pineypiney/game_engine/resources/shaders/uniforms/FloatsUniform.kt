package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class FloatsUniform(
	name: String,
	default: FloatArray = floatArrayOf(),
	getter: UniformGetter<FloatArray> = { floatArrayOf() }
) : Uniform<FloatArray>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setFloats(name, getValue(renderer))
	}
}