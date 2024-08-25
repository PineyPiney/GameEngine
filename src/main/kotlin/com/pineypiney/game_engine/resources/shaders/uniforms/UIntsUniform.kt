package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class UIntsUniform(name: String, default: IntArray = intArrayOf(), getter: UniformGetter<IntArray> = { intArrayOf() }) :
	Uniform<IntArray>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setUInts(name, getValue(renderer))
	}
}