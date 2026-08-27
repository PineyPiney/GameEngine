package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class DoubleUniform(name: String, default: Double = .0, getter: UniformGetter<Double> = { .0 }) :
	Uniform<Double>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setDouble(name, getValue(renderer))
	}
}