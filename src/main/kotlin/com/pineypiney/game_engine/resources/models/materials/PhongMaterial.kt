package com.pineypiney.game_engine.resources.models.materials

import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.util.DeletionQueue
import glm_.pow
import glm_.vec3.Vec3

class PhongMaterial(
	override val name: String,
	val textures: Map<TextureType, Texture2D>,
	val baseColour: Vec3 = Vec3(1),
	val alpha: Float = 1f,
	val shininess: Float = 64f
): ModelMaterial() {

	val deletion = DeletionQueue()

	val mask = textures.keys.sumOf { 2.pow(it.ordinal) }

	val ambient get() = textures[TextureType.AMBIENT] ?: Texture2D.missing
	val diffuse get() = textures[TextureType.DIFFUSE] ?: Texture2D.missing
	val specular get() = textures[TextureType.SPECULAR] ?: Texture2D.missing
	val normals get() = textures[TextureType.NORMAL] ?: Texture2D.missing

	override fun apply(shader: RenderShader, material: String, target: Int) {
		for ((type, texture) in textures) {
			shader.setInt(type.name.lowercase() + "Texture", type.ordinal)
			shader.setTexture(type.ordinal, texture)
		}
		// For Debugging
		shader.setTexture("tex", diffuse)

		shader.setUInt("$material.textureMask", mask.toUInt())
		shader.setBool("$material.ambDiff", true)
		shader.setFloat("$material.shininess", shininess)
		shader.setFloat("$material.alpha", alpha)
	}

	override fun delete() {
		deletion.flush()
	}

	companion object {
		val default = PhongMaterial("default", emptyMap())
	}

	enum class TextureType {
		AMBIENT,
		DIFFUSE,
		SPECULAR,
		NORMAL,
		ROUGHNESS,
		METALLIC
	}
}