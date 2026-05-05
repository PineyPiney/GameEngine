package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.OpenGlShader

class DoublesUniform(
	name: String,
	default: DoubleArray = doubleArrayOf(),
	getter: UniformGetter<DoubleArray> = { doubleArrayOf() }
) : Uniform<DoubleArray>(name, default, getter) {

	override fun apply(shader: OpenGlShader, renderer: RendererI) {
		shader.setDoubles(name, getValue(renderer))
	}
}