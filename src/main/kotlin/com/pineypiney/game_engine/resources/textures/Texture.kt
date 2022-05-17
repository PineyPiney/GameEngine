package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.Resource
import glm_.f
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL46C.*

class Texture(val fileName: String = "", val texturePointer: Int = 0, val width: Int = 0, val height: Int = 0, val numChannels: Int = 0) : Resource() {

    val size = Vec2i(width, height)
    val aspectRatio = width.f/height

    fun bind(){
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, this.texturePointer)
    }

    fun unbind(){
        brokeTexture.bind()
    }

    override fun delete() {
        unbind()
        glDeleteTextures(texturePointer)
    }

    override fun toString(): String {
        return "Texture[$fileName]"
    }

    override fun equals(other: Any?): Boolean {
        if(other is Texture) return this.texturePointer == other.texturePointer
        return false
    }

    override fun hashCode(): Int {
        return this.texturePointer.hashCode()
    }

    companion object {
        val brokeTexture: Texture = Texture("broke", 0, 0, 0, 0)
    }
}