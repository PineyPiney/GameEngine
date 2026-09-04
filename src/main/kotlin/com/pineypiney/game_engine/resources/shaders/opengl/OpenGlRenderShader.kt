package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlMesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderModule
import com.pineypiney.game_engine.resources.shaders.parameters.CullMode
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.RandomHelper

class OpenGlRenderShader(
	ID: Int,
	override val vertex: SubShader,
	override val fragment: SubShader,
	override val stages: List<ShaderModule>,
	uniforms: Map<String, String>,
	override val parameters: RenderShaderParameters
) : OpenGlShader(ID, uniforms), RenderShader {

	override val screenMask: Byte = RandomHelper.createMask(uniforms::containsKey, "view", "projection", "guiProjection", "viewport", "viewPos").toByte()

	override val lightMask: Byte = RandomHelper.createMask(
		uniforms::containsKey,
		"dirLight.ambient",
		"pointLight.ambient",
		"spotLight.ambient"
	).toByte()

	fun setParameters() {
		GLFunc.polygonMode = parameters.fillMode.opengl
		if (parameters.cullMode == CullMode.NONE) GLFunc.cullFace = false
		else {
			GLFunc.cullFace = true
			GLFunc.cullFaceMode = parameters.cullMode.opengl
		}
//		parameters.depthTestOp?.let {
//			GLFunc.depthTest = true
//			GLFunc.depthFunc = it.opengl
//		} ?: run { GLFunc.depthTest = false }

		GLFunc.multiSample = parameters.multisampling != 1
	}

	override fun draw(meshName: String, mesh: Mesh, api: RenderingApi) {
		setParameters()
		(mesh as OpenGlMesh).bindAndDraw(api, parameters.topology.opengl)
	}

	override fun toString(): String {
		return "Shader[${vertex.id}, ${fragment.id}]"
	}
}