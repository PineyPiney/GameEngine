package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.OpenGlShader

class IntUniform(name: String, default: Int = 0, getter: UniformGetter<Int> = { 0 }) :
	Uniform<Int>(name, default, getter) {

	override fun apply(shader: OpenGlShader, renderer: RendererI) {
		shader.setInt(name, getValue(renderer))
	}
}