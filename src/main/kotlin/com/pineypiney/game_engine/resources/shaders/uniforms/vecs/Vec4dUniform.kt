package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.vec4.Vec4d
import glm_.vec4.Vec4t

class Vec4dUniform(name: String, default: Vec4t<*> = Vec4d(), getter: UniformGetter<Vec4t<*>> = { Vec4d() }) :
	Uniform<Vec4t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setVec4d(name, getValue(renderer))
	}
}