package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x3.Mat2x3d

class Mat2x3dsUniform(name: String, default: Array<Mat2x3d> = arrayOf(), getter: (RendererI<*>) -> Array<Mat2x3d>? = {arrayOf()}): Uniform<Array<Mat2x3d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2x3ds(name, getValue(renderer))
	}
}