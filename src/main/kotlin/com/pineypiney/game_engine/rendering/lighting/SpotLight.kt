package com.pineypiney.game_engine.rendering.lighting

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3

class SpotLight(
	var direction: Vec3,
	var cutoff: Float = .9f,
	var outerCutoff: Float = .8f,
	override var ambient: Vec3 = Vec3(0.01f),
	override var diffuse: Vec3 = Vec3(0.8f),
	override var specular: Vec3 = Vec3(1f),
	var constant: Float = 1f,
	var linear: Float = .09f,
	var quadratic: Float = .032f
) : Light() {

	override fun setShaderUniforms(shader: Shader, name: String) {
		super.setShaderUniforms(shader, name)
		shader.setVec3("$name.direction", direction)

		shader.setFloat("$name.cutOff", cutoff)
		shader.setFloat("$name.outerCutOff", outerCutoff)
		shader.setFloat("$name.constant", constant)
		shader.setFloat("$name.linear", linear)
		shader.setFloat("$name.quadratic", quadratic)
	}
}