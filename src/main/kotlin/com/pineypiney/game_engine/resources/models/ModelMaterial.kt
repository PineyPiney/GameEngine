package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.pow
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL33C.*

data class ModelMaterial(val name: String, val textures: Map<TextureType, Texture>, val baseColour: Vec3 = Vec3(1), val alpha: Float = 1f, val shininess: Float = 64f) {

    val mask = textures.keys.sumOf { 2.pow(it.ordinal) }

    val ambient get() = textures[TextureType.AMBIENT] ?: Texture.broke
    val diffuse get() = textures[TextureType.DIFFUSE] ?: Texture.broke
    val specular get() = textures[TextureType.SPECULAR] ?: Texture.broke
    val normals get() = textures[TextureType.NORMAL] ?: Texture.broke

    fun apply(shader: Shader, material: String, target: Int = GL_TEXTURE_2D){
        for((type, texture) in textures){
            shader.setInt(material + "." + type.name.lowercase(), type.ordinal)
            glActiveTexture(0x84C0 + type.ordinal)
            glBindTexture(target, texture.texturePointer)
        }

        shader.setUInt("$material.textureMask", mask.toUInt())
        shader.setBool("$material.ambDiff", true)
        shader.setFloat("$material.shininess", shininess)
        shader.setFloat("$material.alpha", alpha)
    }

    companion object{
        val default = ModelMaterial("default", mapOf(TextureType.DIFFUSE to Texture.broke))
    }

    enum class TextureType(){
        AMBIENT,
        DIFFUSE,
        SPECULAR,
        NORMAL,
        ROUGHNESS,
        METALLIC
    }
}