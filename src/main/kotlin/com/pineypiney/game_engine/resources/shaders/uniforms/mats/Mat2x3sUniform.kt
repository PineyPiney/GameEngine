package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x3.Mat2x3

class Mat2x3sUniform(name: String, default: Array<Mat2x3> = arrayOf(), getter: (RendererI<*>) -> Array<Mat2x3>? = {arrayOf()}): Uniform<Array<Mat2x3>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2x3s(name, getValue(renderer))
	}
}