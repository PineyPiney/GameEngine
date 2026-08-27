package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x2.Mat3x2d

class Mat3x2dUniform(
	name: String,
	default: Mat3x2d = Mat3x2d(DoubleArray(6)),
	getter: UniformGetter<Mat3x2d> = { Mat3x2d(DoubleArray(6)) }
) : Uniform<Mat3x2d>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3x2d(name, getValue(renderer))
	}
}