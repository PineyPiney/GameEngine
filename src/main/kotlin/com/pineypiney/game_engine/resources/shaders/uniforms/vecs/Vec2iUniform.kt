package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.vec2.Vec2t
import glm_.vec2.Vec2i

class Vec2iUniform(name: String, default: Vec2t<*> = Vec2i(), getter: (RendererI<*>) -> Vec2t<*>? = {Vec2i()}): Uniform<Vec2t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setVec2i(name, getValue(renderer))
	}
}