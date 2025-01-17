package com.pineypiney.game_engine.resources.models.materials

import com.pineypiney.game_engine.resources.shaders.Shader
import org.lwjgl.opengl.GL33C.GL_TEXTURE_2D

abstract class ModelMaterial{

	abstract val name: String

	abstract fun apply(shader: Shader, material: String, target: Int = GL_TEXTURE_2D)
}