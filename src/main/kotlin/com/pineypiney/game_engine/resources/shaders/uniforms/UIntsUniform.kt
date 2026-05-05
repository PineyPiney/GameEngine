package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.OpenGlShader

class UIntsUniform(name: String, default: IntArray = intArrayOf(), getter: UniformGetter<IntArray> = { intArrayOf() }) :
	Uniform<IntArray>(name, default, getter) {

	override fun apply(shader: OpenGlShader, renderer: RendererI) {
		shader.setUInts(name, getValue(renderer))
	}
}