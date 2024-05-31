package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.vec4.Vec4t
import glm_.vec4.Vec4ui

class Vec4uiUniform(name: String, default: Vec4t<*> = Vec4ui(), getter: (RendererI<*>) -> Vec4t<*>? = {Vec4ui()}): Uniform<Vec4t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setVec4ui(name, getValue(renderer))
	}
}