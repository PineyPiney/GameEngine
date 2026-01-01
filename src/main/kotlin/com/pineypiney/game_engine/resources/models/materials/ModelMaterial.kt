package com.pineypiney.game_engine.resources.models.materials

import com.pineypiney.game_engine.resources.shaders.RenderShader
import org.lwjgl.opengl.GL33C.GL_TEXTURE_2D

abstract class ModelMaterial{

	abstract val name: String

	abstract fun apply(shader: RenderShader, material: String, target: Int = GL_TEXTURE_2D)
}