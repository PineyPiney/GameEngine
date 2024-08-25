package com.pineypiney.game_engine.resources.shaders

class ShadUn(shader: Shader, val setUniforms: () -> Unit) {

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