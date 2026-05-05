package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.OpenGlShader

class FloatUniform(name: String, default: Float = 0f, getter: UniformGetter<Float> = { 0f }) :
	Uniform<Float>(name, default, getter) {

	override fun apply(shader: OpenGlShader, renderer: RendererI) {
		shader.setFloat(name, getValue(renderer))
	}
}