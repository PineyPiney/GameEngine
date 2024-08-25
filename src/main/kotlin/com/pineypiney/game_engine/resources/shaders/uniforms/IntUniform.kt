package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class IntUniform(name: String, default: Int = 0, getter: UniformGetter<Int> = { 0 }) :
	Uniform<Int>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setInt(name, getValue(renderer))
	}
}