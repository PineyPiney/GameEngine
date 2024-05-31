package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat3x4.Mat3x4

class Mat3x4Uniform(name: String, default: Mat3x4 = Mat3x4(FloatArray(12)), getter: (RendererI<*>) -> Mat3x4? = {Mat3x4(FloatArray(12))}): Uniform<Mat3x4>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat3x4(name, getValue(renderer))
	}
}