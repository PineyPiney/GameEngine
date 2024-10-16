package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.UniformGetter
import glm_.vec3.Vec3t

class Vec3uisUniform(
	name: String,
	default: List<Vec3t<*>> = listOf(),
	getter: UniformGetter<List<Vec3t<*>>> = { listOf() }
) : Uniform<List<Vec3t<*>>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setVec3uis(name, getValue(renderer))
	}
}