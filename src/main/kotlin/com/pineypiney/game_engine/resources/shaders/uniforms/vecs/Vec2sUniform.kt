package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.vec2.Vec2t

class Vec2sUniform(name: String, default: List<Vec2t<*>> = listOf(), getter: (RendererI<*>) -> List<Vec2t<*>>? = {listOf()}): Uniform<List<Vec2t<*>>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setVec2s(name, getValue(renderer))
	}
}