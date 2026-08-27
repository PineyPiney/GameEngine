package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x2.Mat3x2d

class Mat3x2dsUniform(
	name: String,
	default: Array<Mat3x2d> = arrayOf(),
	getter: UniformGetter<Array<Mat3x2d>> = { arrayOf() }
) : Uniform<Array<Mat3x2d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3x2ds(name, getValue(renderer))
	}
}