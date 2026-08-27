package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x4.Mat3x4

class Mat3x4sUniform(
	name: String,
	default: Array<Mat3x4> = arrayOf(),
	getter: UniformGetter<Array<Mat3x4>> = { arrayOf() }
) : Uniform<Array<Mat3x4>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3x4s(name, getValue(renderer))
	}
}