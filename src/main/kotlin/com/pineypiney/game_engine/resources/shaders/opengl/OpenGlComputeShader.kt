package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.shaders.ComputeShader
import org.lwjgl.opengl.GL43C.glDispatchCompute

class OpenGlComputeShader(ID: Int, override val compute: SubShader, uniforms: Map<String, String>) : OpenGlShader(ID, uniforms), ComputeShader {

	override fun dispatch(api: RenderingApi, x: Int, y: Int, z: Int) {
		glDispatchCompute(x, y, z)
	}

	override fun toString(): String {
		return "Shader[${compute.getName()}]"
	}
}