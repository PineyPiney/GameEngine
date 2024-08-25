package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x3.Mat3

class Mat3sUniform(name: String, default: Array<Mat3> = arrayOf(), getter: UniformGetter<Array<Mat3>> = { arrayOf() }) :
	Uniform<Array<Mat3>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3s(name, getValue(renderer))
	}
}