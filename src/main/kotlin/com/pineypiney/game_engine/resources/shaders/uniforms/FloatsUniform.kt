package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.OpenGlShader

class FloatsUniform(
	name: String,
	default: FloatArray = floatArrayOf(),
	getter: UniformGetter<FloatArray> = { floatArrayOf() }
) : Uniform<FloatArray>(name, default, getter) {

	override fun apply(shader: OpenGlShader, renderer: RendererI) {
		shader.setFloats(name, getValue(renderer))
	}
}