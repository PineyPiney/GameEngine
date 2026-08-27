package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class LongUniform(name: String, default: Long = 0L, getter: UniformGetter<Long> = { 0 }) :
	Uniform<Long>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setLong(name, getValue(renderer))
	}
}