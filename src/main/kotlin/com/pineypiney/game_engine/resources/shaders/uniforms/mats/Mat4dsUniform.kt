package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat4x4.Mat4d

class Mat4dsUniform(name: String, default: Array<Mat4d> = arrayOf(), getter: (RendererI<*>) -> Array<Mat4d>? = {arrayOf()}): Uniform<Array<Mat4d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat4ds(name, getValue(renderer))
	}
}