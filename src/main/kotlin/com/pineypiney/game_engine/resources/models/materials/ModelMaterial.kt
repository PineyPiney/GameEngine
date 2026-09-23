package com.pineypiney.game_engine.resources.models.materials

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.RenderShader
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL33C.GL_TEXTURE_2D

abstract class ModelMaterial : Deletable {

	abstract val name: String
	abstract val baseColour: Vec4

	abstract fun apply(shader: RenderShader, material: String, target: Int = GL_TEXTURE_2D)

	companion object {
		val missing = PhongMaterial("broke", mapOf())
	}
}