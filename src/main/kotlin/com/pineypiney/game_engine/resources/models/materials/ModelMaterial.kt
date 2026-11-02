package com.pineypiney.game_engine.resources.models.materials

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.RenderShader
import glm_.vec4.Vec4

abstract class ModelMaterial : Deletable {

	abstract val name: String
	abstract val baseColour: Vec4

	abstract fun apply(shader: RenderShader, material: String)

	companion object {
		val missing = PhongMaterial("broke", mapOf())
	}
}