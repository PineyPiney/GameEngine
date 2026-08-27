package com.pineypiney.game_engine.resources.models.materials

import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.util.extension_functions.delete
import glm_.vec4.Vec4

class PBRMaterial(
	override val name: String, val textures: Map<String, Texture2D>, val baseColour: Vec4 = Vec4(1f),
	var metallicness: Float = 0f, var roughness: Float = .5f, val emission: Float = 1f,
	var sheen: Float = 0f, var sheenTint: Float = .5f, var anisotropic: Float = 0f,
	var specular: Float = .5f, var specTint: Float = 0f
) : ModelMaterial() {

	val mask = createTextureMask()

	override fun apply(shader: RenderShader, material: String, target: Int) {

		textures.onEachIndexed { i, (type, texture) ->
			shader.setTexture(type, texture)
//			shader.setInt("$material.$type", i)
//			glActiveTexture(0x84C0 + i)
//			if (texture is OpenGlTexture) glBindTexture(target, texture.texturePointer)
		}

		shader.setUInt("$material.textureMask", mask.toUInt())

		shader.setVec4("$material.baseColourFactor", baseColour)
		shader.setFloat("$material.metallicFactor", metallicness)
		shader.setFloat("$material.roughnessFactor", roughness)
		shader.setFloat("$material.emissiveFactor", emission)

		shader.setFloat("$material.sheen", sheen)
		shader.setFloat("$material.sheenTint", sheenTint)
		shader.setFloat("$material.anisotropic", anisotropic)
		shader.setFloat("$material.specular", specular)
		shader.setFloat("$material.specTint", specTint)

		shader.setFloat("$material.alpha", 1f)
	}

	fun createTextureMask(): Byte{
		var i = if(textures.containsKey("baseColour")) 1 else 0
		if(textures.containsKey("metallicRoughness")) i += 2
		if(textures.containsKey("normals")) i += 4
		if(textures.containsKey("occlusion")) i += 8
		if(textures.containsKey("emissive")) i += 16
		return i.toByte()
	}

	override fun delete() {
		textures.delete()
	}

	companion object {
		val default = PBRMaterial("default", mapOf("baseColour" to Texture2D.missing))
	}
}