package com.pineypiney.game_engine.resources.shaders

class ShadUn<S : OpenGlShader>(shader: S, val setUniforms: () -> Unit) {

	var shader = shader
		set(value) {
			field = value
			uniforms = value.compileUniforms()
		}

	var uniforms = shader.compileUniforms()
		set(value) {
			field = value
			setUniforms()
		}
}