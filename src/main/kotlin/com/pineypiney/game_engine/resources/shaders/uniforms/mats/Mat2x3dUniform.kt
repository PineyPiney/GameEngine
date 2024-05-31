package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x3.Mat2x3d

class Mat2x3dUniform(name: String, default: Mat2x3d = Mat2x3d(DoubleArray(6)), getter: (RendererI<*>) -> Mat2x3d? = {Mat2x3d(DoubleArray(6))}): Uniform<Mat2x3d>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2x3d(name, getValue(renderer))
	}
}