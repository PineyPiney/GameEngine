package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat4x3.Mat4x3d

class Mat4x3dsUniform(
	name: String,
	default: Array<Mat4x3d> = arrayOf(),
	getter: UniformGetter<Array<Mat4x3d>> = { arrayOf() }
) : Uniform<Array<Mat4x3d>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat4x3ds(name, getValue(renderer))
	}
}