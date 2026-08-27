package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.resources.shaders.ShaderModule
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import org.lwjgl.opengl.GL20C

class SubShader(val id: String, private val stage: ShaderStage, val handle: Int, val uniforms: Map<String, String>) : ShaderModule {

	override fun getName(): String = id
	override fun getStage(): ShaderStage = stage

	override fun delete() {
		GL20C.glDeleteShader(handle)
	}
}