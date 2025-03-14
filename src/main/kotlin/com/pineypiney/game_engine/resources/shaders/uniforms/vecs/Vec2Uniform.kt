package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.vec2.Vec2
import glm_.vec2.Vec2t

class Vec2Uniform(name: String, default: Vec2t<*> = Vec2(), getter: UniformGetter<Vec2t<*>> = { Vec2() }) :
	Uniform<Vec2t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setVec2(name, getValue(renderer))
	}
}