package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.resources.Resource
import org.lwjgl.opengl.GL20C

class SubShader(val id: Int, val uniforms: Map<String, String>) : Resource() {

	override fun delete() {
		GL20C.glDeleteShader(id)
	}
}