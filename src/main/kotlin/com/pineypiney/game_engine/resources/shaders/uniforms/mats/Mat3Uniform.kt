package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x3.Mat3

class Mat3Uniform(name: String, default: Mat3 = Mat3(), getter: UniformGetter<Mat3> = { Mat3() }) :
	Uniform<Mat3>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3(name, getValue(renderer))
	}
}