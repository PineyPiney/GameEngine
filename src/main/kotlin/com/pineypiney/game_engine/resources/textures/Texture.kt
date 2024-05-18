package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.f
import glm_.vec2.Vec2i
import kool.Buffer
import kool.ByteBuffer
import kool.lim
import kool.toBuffer
import org.lwjgl.opengl.GL32C.*
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

class Texture(val fileLocation: String, val texturePointer: Int, val target: Int = GL_TEXTURE_2D, var binding: Int = 0) : Resource() {

    val fileName = fileLocation.substringAfterLast('\\').substringBefore('.')

    val width: Int get() = parameter(GL_TEXTURE_WIDTH)
    val height: Int get() = parameter(GL_TEXTURE_HEIGHT)
    val format: Int get() = parameter(GL_TEXTURE_INTERNAL_FORMAT)
    val numChannels: Int get() = TextureLoader.formatToChannels(format)
    val bytes: Int get() = width * height * numChannels

    val size get() = Vec2i(width, height)
    val aspectRatio get() = width.f/height


    fun bind(){
        glActiveTexture(GL_TEXTURE0 + binding)
        glBindTexture(target, texturePointer)
    }

    fun unbind(){
        broke.bind()
    }

    fun setData(data: ByteBuffer, x: Int = 0, y: Int = 0, width: Int = this.width, height: Int = this.height, format: Int = this.format){
        bind()
        if(data.lim != width * height * numChannels){
            GameEngineI.warn("Buffer is not the right size to set texture data")
        }
        val buf = Buffer(data.lim){ data.get(it) }
        glTexSubImage2D(target, 0, x, y, width, height, format, GL_UNSIGNED_BYTE, buf)
    }

    fun getData(): ByteBuffer{
        bind()
        val buffer = ByteBuffer(bytes)
        glGetTexImage(target, 0, format, GL_UNSIGNED_BYTE, buffer)
        return buffer
    }

    fun setSamples(samples: Int, fixedSample: Boolean = true){
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texturePointer)
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format, width, height, fixedSample)
    }

    fun parameter(param: Int): Int{
        return if(GLFunc.isLoaded){
            bind()
            glGetTexLevelParameteri(target, 0, param)
        }
        else 0
    }

    fun savePNG(file: CharSequence): Boolean{
        val d = getData().rewind().flip()
        d.limit(d.capacity())
        return STBImageWrite.stbi_write_png(file, width, height, numChannels, d, numChannels * width)
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
        val broke: Texture = Texture("missing", TextureLoader.createTexture(createArray().toBuffer(), 32, 32))

        fun createArray(): ByteArray{
            val b = byteArrayOf(0, 0, 0)
            val m = byteArrayOf(-1, 0, -1)
            val row = (m repeat 16) + (b repeat 16)
            val row2 = (b repeat 16) + (m repeat 16)
            return (row repeat 16) + (row2 repeat 16)
        }
    }
}