package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.vec3.Vec3t
import glm_.vec3.Vec3

class Vec3Uniform(name: String, default: Vec3t<*> = Vec3(), getter: (RendererI<*>) -> Vec3t<*>? = {Vec3()}): Uniform<Vec3t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setVec3(name, getValue(renderer))
	}
}