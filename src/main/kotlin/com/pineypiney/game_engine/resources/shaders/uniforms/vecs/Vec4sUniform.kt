package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.vec4.Vec4t

class Vec4sUniform(name: String, default: List<Vec4t<*>> = listOf(), getter: (RendererI<*>) -> List<Vec4t<*>>? = {listOf()}): Uniform<List<Vec4t<*>>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setVec4s(name, getValue(renderer))
	}
}