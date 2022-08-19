package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.f
import glm_.vec2.Vec2i
import kool.ByteBuffer
import org.lwjgl.opengl.GL46C.*
import java.nio.ByteBuffer

class Texture(val fileName: String = "", val texturePointer: Int = 0, val width: Int = 0, val height: Int = 0) : Resource() {

    val size = Vec2i(width, height)
    val aspectRatio = width.f/height

    val format: Int get(){
        bind()
        return glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT)
    }

    val numChannels: Int get() = TextureLoader.formatToChannels[format] ?: 3

    fun bind(){
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, this.texturePointer)
    }

    fun unbind(){
        brokeTexture.bind()
    }

    fun getData(): ByteBuffer{

        bind()
        val buffer = ByteBuffer(width * height * numChannels)
        glGetTexImage(GL_TEXTURE_2D, 0, format, GL_BYTE, buffer)
        return buffer
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
        val brokeTexture: Texture = Texture("broke", 0, 0, 0)
        val missing = Texture("missing", TextureLoader.createTexture(createArray(), 32, 32), 32, 32)

        fun createArray(): ByteArray{
            val b = byteArrayOf(0, 0, 0)
            val m = byteArrayOf(-1, 0, -1)
            val row = (m repeat 16) + (b repeat 16)
            val row2 = (b repeat 16) + (m repeat 16)
            return (row repeat 16) + (row2 repeat 16)
        }
    }
}