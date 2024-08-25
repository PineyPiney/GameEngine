package com.pineypiney.game_engine.rendering.lighting

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3

class PointLight(
	override val ambient: Vec3 = Vec3(0.1f),
	override val diffuse: Vec3 = Vec3(0.5f),
	override val specular: Vec3 = Vec3(1f),
	val constant: Float = 1f,
	val linear: Float = .09f,
	val quadratic: Float = .032f
) : Light() {

	override fun setShaderUniforms(shader: Shader, name: String) {
		super.setShaderUniforms(shader, name)
		shader.setFloat("$name.constant", constant)
		shader.setFloat("$name.linear", linear)
		shader.setFloat("$name.quadratic", quadratic)
	}

	companion object {
		fun setShaderUniforms(shader: Shader, name: String, lights: Map<Vec3, PointLight>) {
			Light.setShaderUniforms(shader, name, lights)
			shader.setFloats("$name.constant", lights.values.map { it.constant }.toFloatArray())
			shader.setFloats("$name.linear", lights.values.map { it.linear }.toFloatArray())
			shader.setFloats("$name.quadratic", lights.values.map { it.quadratic }.toFloatArray())
		}
	}
}