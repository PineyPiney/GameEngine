package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat4x2.Mat4x2

class Mat4x2sUniform(
	name: String,
	default: Array<Mat4x2> = arrayOf(),
	getter: UniformGetter<Array<Mat4x2>> = { arrayOf() }
) : Uniform<Array<Mat4x2>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat4x2s(name, getValue(renderer))
	}
}