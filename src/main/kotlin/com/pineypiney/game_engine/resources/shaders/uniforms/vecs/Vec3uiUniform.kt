package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.vec3.Vec3t
import glm_.vec3.Vec3ui

class Vec3uiUniform(name: String, default: Vec3t<*> = Vec3ui(), getter: UniformGetter<Vec3t<*>> = { Vec3ui() }) :
	Uniform<Vec3t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setVec3ui(name, getValue(renderer))
	}
}