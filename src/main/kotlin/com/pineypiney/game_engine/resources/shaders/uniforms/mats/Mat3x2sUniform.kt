package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat3x2.Mat3x2

class Mat3x2sUniform(name: String, default: Array<Mat3x2> = arrayOf(), getter: (RendererI<*>) -> Array<Mat3x2>? = {arrayOf()}): Uniform<Array<Mat3x2>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat3x2s(name, getValue(renderer))
	}
}