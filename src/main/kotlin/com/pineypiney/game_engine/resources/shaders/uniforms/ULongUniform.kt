package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class ULongUniform(name: String, default: ULong = 0uL, getter: UniformGetter<ULong> = { 0uL }) :
	Uniform<ULong>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setULong(name, getValue(renderer))
	}
}