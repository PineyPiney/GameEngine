package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class BoolUniform(name: String, default: Boolean = false, getter: UniformGetter<Boolean> = { false }) :
	Uniform<Boolean>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setBool(name, getValue(renderer))
	}
}