package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.mat4x4.Mat4

class Mat4Uniform(name: String, default: Mat4 = Mat4(), getter: UniformGetter<Mat4> = { Mat4() }) :
	Uniform<Mat4>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setMat4(name, getValue(renderer))
	}
}