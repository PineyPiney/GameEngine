package com.pineypiney.game_engine.resources.shaders.uniforms.vecs

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.vec3.Vec3d
import glm_.vec3.Vec3t

class Vec3dUniform(name: String, default: Vec3t<*> = Vec3d(), getter: (RendererI<*>) -> Vec3t<*>? = {Vec3d()}): Uniform<Vec3t<*>>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI<*>) {
		shader.setVec3d(name, getValue(renderer))
	}
}