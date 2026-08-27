package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat4x3.Mat4x3d

class Mat4x3dUniform(
	name: String,
	default: Mat4x3d = Mat4x3d(DoubleArray(12)),
	getter: UniformGetter<Mat4x3d> = { Mat4x3d(DoubleArray(12)) }
) : Uniform<Mat4x3d>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat4x3d(name, getValue(renderer))
	}
}