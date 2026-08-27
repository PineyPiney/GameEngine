package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.vec4.Vec4
import glm_.vec4.Vec4t

class Vec4Uniform(name: String, default: Vec4t<*> = Vec4(), getter: UniformGetter<Vec4t<*>> = { Vec4() }) :
	Uniform<Vec4t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setVec4(name, getValue(renderer))
	}
}