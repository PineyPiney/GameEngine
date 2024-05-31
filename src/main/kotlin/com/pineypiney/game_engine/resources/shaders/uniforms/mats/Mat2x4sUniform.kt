package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x4.Mat2x4

class Mat2x4sUniform(name: String, default: Array<Mat2x4> = arrayOf(), getter: (RendererI<*>) -> Array<Mat2x4>? = {arrayOf()}): Uniform<Array<Mat2x4>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2x4s(name, getValue(renderer))
	}
}