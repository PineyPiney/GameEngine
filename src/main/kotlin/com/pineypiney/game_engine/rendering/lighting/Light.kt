package com.pineypiney.game_engine.rendering.lighting

import com.pineypiney.game_engine.resources.shaders.RenderShader
import glm_.vec3.Vec3

abstract class Light {

	var on = true

	abstract val ambient: Vec3
	abstract val diffuse: Vec3
	abstract val specular: Vec3

	open fun setShaderUniforms(shader: RenderShader, name: String) {
		shader.setVec3("$name.diffuse", diffuse)
		shader.setVec3("$name.ambient", ambient)
		shader.setVec3("$name.specular", specular)
	}

	companion object {

		fun setShaderUniforms(shader: RenderShader, name: String, lights: Map<Vec3, Light>) {
			shader.setVec3s("$name.position", lights.keys.toList())
			shader.setVec3s("$name.ambient", lights.values.map { it.ambient })
			shader.setVec3s("$name.diffuse", lights.values.map { it.diffuse })
			shader.setVec3s("$name.specular", lights.values.map { it.specular })
		}


		fun setShaderUniformsOff(shader: RenderShader, name: String) {
			shader.setVec3("$name.diffuse", Vec3(0f))
			shader.setVec3("$name.ambient", Vec3(0f))
			shader.setVec3("$name.specular", Vec3(0f))
		}

	}
}