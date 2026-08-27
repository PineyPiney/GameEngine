package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x4.Mat3x4d

class Mat3x4dsUniform(
	name: String,
	default: Array<Mat3x4d> = arrayOf(),
	getter: UniformGetter<Array<Mat3x4d>> = { arrayOf() }
) : Uniform<Array<Mat3x4d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3x4ds(name, getValue(renderer))
	}
}