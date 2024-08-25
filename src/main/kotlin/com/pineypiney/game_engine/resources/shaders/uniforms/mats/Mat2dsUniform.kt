package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat2x2.Mat2d

class Mat2dsUniform(
	name: String,
	default: Array<Mat2d> = arrayOf(),
	getter: UniformGetter<Array<Mat2d>> = { arrayOf() }
) : Uniform<Array<Mat2d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat2ds(name, getValue(renderer))
	}
}