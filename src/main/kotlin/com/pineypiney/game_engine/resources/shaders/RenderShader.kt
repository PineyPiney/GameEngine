package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.LightComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.resources.shaders.uniforms.*
import com.pineypiney.game_engine.resources.shaders.uniforms.mats.*
import com.pineypiney.game_engine.resources.shaders.uniforms.vecs.*
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.RandomHelper
import glm_.b
import glm_.i
import glm_.mat2x2.Mat2
import glm_.mat2x2.Mat2d
import glm_.mat2x3.Mat2x3
import glm_.mat2x3.Mat2x3d
import glm_.mat2x4.Mat2x4
import glm_.mat2x4.Mat2x4d
import glm_.mat3x2.Mat3x2
import glm_.mat3x2.Mat3x2d
import glm_.mat3x3.Mat3
import glm_.mat3x3.Mat3d
import glm_.mat3x4.Mat3x4
import glm_.mat3x4.Mat3x4d
import glm_.mat4x2.Mat4x2
import glm_.mat4x2.Mat4x2d
import glm_.mat4x3.Mat4x3
import glm_.mat4x3.Mat4x3d
import glm_.mat4x4.Mat4
import glm_.mat4x4.Mat4d
import glm_.vec2.*
import glm_.vec3.*
import glm_.vec4.*
import org.lwjgl.opengl.GL46C
import org.lwjgl.opengl.GL46C.*
import kotlin.experimental.and

@Suppress("UNUSED")
class RenderShader(
	ID: Int,
	val vName: String,
	val fName: String,
	val gName: String? = null,
	uniforms: Map<String, String>
) : Shader(ID, uniforms) {

	val screenMask: Byte =
		RandomHelper.createMask(uniforms::containsKey, "view", "projection", "guiProjection", "viewport", "viewPos").b

	val hasView get() = (screenMask and 1) > 0
	val hasProj get() = (screenMask and 2) > 0
	val hasGUI get() = (screenMask and 4) > 0
	val hasPort get() = (screenMask and 8) > 0
	val hasPos get() = (screenMask and 0x10) > 0

	val lightMask: Byte = RandomHelper.createMask(
		uniforms::containsKey,
		"dirLight.ambient",
		"pointLight.ambient",
		"spotLight.ambient"
	).b

	val hasDirL get() = (lightMask and 1) > 0
	val hasPointL get() = (lightMask and 2) > 0
	val hasSpotL get() = (lightMask and 4) > 0

	fun setRendererDefaults(uniforms: Uniforms){
		if (hasView) uniforms.setMat4UniformR("view", RendererI::view)
		if (hasProj) uniforms.setMat4UniformR("projection", RendererI::projection)
		if (hasGUI) uniforms.setMat4UniformR("guiProjection", RendererI::guiProjection)
		if (hasPort) uniforms.setVec2iUniformR("viewport", RendererI::viewportSize)
		if (hasPos) uniforms.setVec3UniformR("viewPos", RendererI::viewPos)
	}

	fun setLightUniforms(obj: GameObject, lights: List<LightComponent> = obj.objects?.getAllComponents()?.filterIsInstance<LightComponent>()?.filter { it.light.on } ?: emptyList()) {
		if(lights.isEmpty()) return

		val dirLight = lights.firstOrNull { it.light is DirectionalLight }
		if(dirLight == null) Light.setShaderUniformsOff(this, "dirLight")
		else dirLight.setShaderUniforms(this, "dirLight")

		val pointLights = lights.associateWith { it.parent.position }.filter{ it.key.light is PointLight }.entries.sortedByDescending { (it.value - obj.position).length() / (it.key.light as PointLight).linear }
		for (l in 0..<4) {
			val name = "pointLights[$l]"
			if(l < pointLights.size) pointLights[l].key.setShaderUniforms(this, name)
			else Light.setShaderUniformsOff(this, name)
		}

		val spotLight = lights.firstOrNull { it.light is SpotLight }
		if(spotLight == null) Light.setShaderUniformsOff(this, "spotlight")
		else spotLight.setShaderUniforms(this, "spotlight")
	}

	override fun toString(): String {
		return "Shader[$vName, $fName]"
	}

	companion object {

		val vS: String
		val fS: String

		init {
			val (V, v) = GLFunc.version
			vS =
				"#version $V${v}0 core\n" +
						"layout (location = 0) in vec2 aPos;\n" +
						"\n" +
						"void main(){\n" +
						"\tgl_Position = vec4(aPos, 0.0, 1.0);\n" +
						"}"
			fS =
				"#version $V${v}0 core\n" +
						"\n" +
						"out vec4 FragColour;\n" +
						"\n" +
						"void main(){\n" +
						"\tFragColour = vec4(1.0, 1.0, 1.0, 1.0);\n" +
						"}"
		}

		val brokeShader: RenderShader = ShaderLoader.generateShader(
			"broke", ShaderLoader.generateSubShader("broke", vS, GL_VERTEX_SHADER),
			"broke", ShaderLoader.generateSubShader("broke", fS, GL_FRAGMENT_SHADER)
		)
	}
}