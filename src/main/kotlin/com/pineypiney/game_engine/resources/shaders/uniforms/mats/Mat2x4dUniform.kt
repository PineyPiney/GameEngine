package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x4.Mat2x4d

class Mat2x4dUniform(name: String, default: Mat2x4d = Mat2x4d(DoubleArray(8)), getter: (RendererI<*>) -> Mat2x4d? = {Mat2x4d(DoubleArray(8))}): Uniform<Mat2x4d>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2x4d(name, getValue(renderer))
	}
}