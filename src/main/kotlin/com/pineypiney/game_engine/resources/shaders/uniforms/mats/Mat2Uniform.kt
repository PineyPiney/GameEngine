package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat2x2.Mat2

class Mat2Uniform(name: String, default: Mat2 = Mat2(), getter: (RendererI<*>) -> Mat2? = {Mat2()}): Uniform<Mat2>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat2(name, getValue(renderer))
	}
}