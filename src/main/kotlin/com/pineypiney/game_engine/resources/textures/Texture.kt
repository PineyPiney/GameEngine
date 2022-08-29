package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.f
import glm_.vec2.Vec2i
import kool.Buffer
import kool.ByteBuffer
import kool.lim
import org.lwjgl.opengl.GL13.*
import java.nio.ByteBuffer

class Texture(val fileName: String, val texturePointer: Int) : Resource() {

    val width: Int get() = parameter(GL_TEXTURE_WIDTH)
    val height: Int get() = parameter(GL_TEXTURE_HEIGHT)
    val format: Int get() = parameter(GL_TEXTURE_INTERNAL_FORMAT)
    val numChannels: Int get() = TextureLoader.formatToChannels(format)

    val size get() = Vec2i(width, height)
    val aspectRatio get() = width.f/height


    fun bind(){
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texturePointer)
    }

    fun unbind(){
        broke.bind()
    }

    fun setData(data: ByteBuffer, x: Int = 0, y: Int = 0, width: Int = this.width, height: Int = this.height, format: Int = this.format){
        bind()
        if(data.lim != width * height * numChannels){
            GameEngine.logger.warn("Buffer is not the right size to set texture data")
        }
        val buf = Buffer(data.lim){ data.get(it) }
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, format, GL_UNSIGNED_BYTE, buf)
    }

    fun getData(): ByteBuffer{

        bind()
        val buffer = ByteBuffer(width * height * numChannels)
        glGetTexImage(GL_TEXTURE_2D, 0, format, GL_UNSIGNED_BYTE, buffer)
        return buffer
    }

    fun parameter(param: Int): Int{
        bind()
        return glGetTexLevelParameteri(GL_TEXTURE_2D, 0, param)
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
        val broke: Texture = Texture("missing", TextureLoader.createTexture(createArray(), 32, 32))

        fun createArray(): ByteArray{
            val b = byteArrayOf(0, 0, 0)
            val m = byteArrayOf(-1, 0, -1)
            val row = (m repeat 16) + (b repeat 16)
            val row2 = (b repeat 16) + (m repeat 16)
            return (row repeat 16) + (row2 repeat 16)
        }
    }
}