package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat3x3.Mat3d

class Mat3dsUniform(name: String, default: Array<Mat3d> = arrayOf(), getter: (RendererI<*>) -> Array<Mat3d>? = {arrayOf()}): Uniform<Array<Mat3d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat3ds(name, getValue(renderer))
	}
}