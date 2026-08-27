package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class FloatUniform(name: String, default: Float = 0f, getter: UniformGetter<Float> = { 0f }) :
	Uniform<Float>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setFloat(name, getValue(renderer))
	}
}