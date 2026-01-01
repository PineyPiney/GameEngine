package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.util.GLFunc
import glm_.vec3.Vec3i
import org.lwjgl.opengl.GL46C.GL_VERTEX_SHADER
import org.lwjgl.opengl.GL46C.glDispatchCompute

@Suppress("UNUSED")
class ComputeShader(
	ID: Int,
	val name: String,
	uniforms: Map<String, String>
) : Shader(ID, uniforms) {

	fun dispatch(x: Int = 1, y: Int = 1, z: Int = 1){
		glDispatchCompute(x, y, z)
	}

	fun dispatch(groups: Vec3i){
		glDispatchCompute(groups.x, groups.y, groups.z)
	}

	override fun toString(): String {
		return "Shader[$name]"
	}

	companion object {

		val cS: String

		init {
			val (V, v) = GLFunc.version
			cS =
				"#version $V${v}0 core\n" +
						"layout (location = 0) in vec2 aPos;\n" +
						"\n" +
						"void main(){\n" +
						"\tgl_Position = vec4(aPos, 0.0, 1.0);\n" +
						"}"
		}

		val brokeShader: ComputeShader = ShaderLoader.generateComputeShader(
			"broke", ShaderLoader.generateSubShader("broke", cS, GL_VERTEX_SHADER)
		)
	}
}