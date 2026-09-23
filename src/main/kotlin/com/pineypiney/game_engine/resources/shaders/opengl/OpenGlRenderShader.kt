package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlMesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderModule
import com.pineypiney.game_engine.resources.shaders.parameters.Blending
import com.pineypiney.game_engine.resources.shaders.parameters.CullMode
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.RandomHelper
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i

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

		GLFunc.cullFace = parameters.cullMode != CullMode.NONE
		if (parameters.cullMode != CullMode.NONE) GLFunc.cullFaceMode = parameters.cullMode.opengl

		parameters.depthTestOp?.let {
			GLFunc.depthTest = true
			GLFunc.depthFunc = it.opengl
		} ?: run { GLFunc.depthTest = false }

		parameters.blending?.let { (src, dst, op) ->
			GLFunc.blend = true

			if (src is Blending.Same && dst is Blending.Same) GLFunc.blendFactors = Vec2i(src.value.opengl, dst.value.opengl)
			else GLFunc.blendFactorsSeparate = Vec4i(src.colour().opengl, dst.colour().opengl, src.alpha().opengl, dst.alpha().opengl)

			when (op) {
				is Blending.Separate -> GLFunc.blendEquationSeparate = Vec2i(op.rgb.opengl, op.a.opengl)
				is Blending.Same -> GLFunc.blendEquation = op.value.opengl
			}
		} ?: run { GLFunc.blend = false }

		if (parameters.tesselation > 0) GLFunc.patchVertices = parameters.tesselation

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