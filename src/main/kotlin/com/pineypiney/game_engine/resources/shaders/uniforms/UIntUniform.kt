package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.OpenGlShader

class UIntUniform(name: String, default: UInt = 0u, getter: UniformGetter<UInt> = { 0u }) :
	Uniform<UInt>(name, default, getter) {

	override fun apply(shader: OpenGlShader, renderer: RendererI) {
		shader.setUInt(name, getValue(renderer))
	}
}