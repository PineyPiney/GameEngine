package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x2.Mat2d

class Mat2dUniform(name: String, default: Mat2d = Mat2d(), getter: (RendererI<*>) -> Mat2d? = {Mat2d()}): Uniform<Mat2d>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2d(name, getValue(renderer))
	}
}