package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat2x3.Mat2x3

class Mat2x3Uniform(
	name: String,
	default: Mat2x3 = Mat2x3(FloatArray(6)),
	getter: UniformGetter<Mat2x3> = { Mat2x3(FloatArray(6)) }
) : Uniform<Mat2x3>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat2x3(name, getValue(renderer))
	}
}