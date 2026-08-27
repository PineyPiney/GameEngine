package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat4x3.Mat4x3

class Mat4x3sUniform(
	name: String,
	default: Array<Mat4x3> = arrayOf(),
	getter: UniformGetter<Array<Mat4x3>> = { arrayOf() }
) : Uniform<Array<Mat4x3>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat4x3s(name, getValue(renderer))
	}
}