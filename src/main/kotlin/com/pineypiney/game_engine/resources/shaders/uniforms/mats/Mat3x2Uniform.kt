package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat3x2.Mat3x2

class Mat3x2Uniform(name: String, default: Mat3x2 = Mat3x2(FloatArray(6)), getter: (RendererI<*>) -> Mat3x2? = {Mat3x2(FloatArray(6))}): Uniform<Mat3x2>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat3x2(name, getValue(renderer))
	}
}