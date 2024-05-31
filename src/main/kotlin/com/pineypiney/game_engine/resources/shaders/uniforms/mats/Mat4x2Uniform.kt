package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat4x2.Mat4x2

class Mat4x2Uniform(name: String, default: Mat4x2 = Mat4x2(FloatArray(8)), getter: (RendererI<*>) -> Mat4x2? = {Mat4x2(FloatArray(8))}): Uniform<Mat4x2>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setMat4x2(name, getValue(renderer))
	}
}