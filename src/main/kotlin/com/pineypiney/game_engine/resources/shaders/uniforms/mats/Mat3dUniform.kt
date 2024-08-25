package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat3x3.Mat3d

class Mat3dUniform(name: String, default: Mat3d = Mat3d(), getter: UniformGetter<Mat3d> = { Mat3d() }) :
	Uniform<Mat3d>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat3d(name, getValue(renderer))
	}
}