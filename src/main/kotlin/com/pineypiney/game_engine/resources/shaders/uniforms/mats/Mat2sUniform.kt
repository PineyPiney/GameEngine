package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat2x2.Mat2

class Mat2sUniform(name: String, default: Array<Mat2> = arrayOf(), getter: UniformGetter<Array<Mat2>> = { arrayOf() }) :
	Uniform<Array<Mat2>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat2s(name, getValue(renderer))
	}
}