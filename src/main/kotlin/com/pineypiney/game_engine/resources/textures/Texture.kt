package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.resources.ResourceLoader
import com.pineypiney.game_engine.util.ResourceKey
import kool.lim
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.stb.STBImage.*
import java.io.InputStream
import java.nio.ByteBuffer

class Texture(val stream: InputStream, val fileName: String = "", val wrapping: Int = GL_REPEAT, val flip: Boolean = true) : Resource() {

    var width = 0; private set
    var height = 0; private set
    var numChannels = 0; private set

    var texturePointer = 0; private set

    init{
        if(stream.available() < 1){
            println("InputStream for $fileName is empty")
            this.texturePointer = brokeTexture.texturePointer
        }
        else {
            loadTextureSettings()
            // Load the image from file
            loadImage(ResourceLoader.ioResourceToByteBuffer(stream, 1024))
        }
    }

    private fun loadTextureSettings(){
        // Create a handle for the texture
        this.texturePointer = glGenTextures()

        // Settings
        loadIndividualSettings(this, wrapping)

        // Set the flip state of the image (default to true)
        stbi_set_flip_vertically_on_load(flip)
    }

    private fun loadImage(buffer: ByteBuffer) {

        // Arrays are Java equivalent for pointers
        val widths = IntArray(1)
        val heights = IntArray(1)
        val numChannels = IntArray(1)

        // Load texture data from file
        val data: ByteBuffer? = stbi_load_from_memory(buffer, widths, heights, numChannels, 0)
        if (data != null) {

            this.width = widths[0]
            this.height = heights[0]
            this.numChannels = numChannels[0]

            val format = when(this.numChannels) {
                1 -> GL_RED
                3 -> GL_RGB
                4 -> GL_RGBA
                else -> GL_GREEN
            }

            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data)

            val array = ByteArray(data.lim)
            data.get(array)
            glGenerateMipmap(GL_TEXTURE_2D)

            stbi_image_free(data)
        }
        else {
            println("\nFailed to load texture at $fileName")
            // loadImage("${textureDir}broke.png")
        }

        buffer.clear()
    }

    fun loadIndividualSettings(texture: Texture, wrapping: Int = GL_REPEAT) {
        // Bind the texture so that future operations are on this texture
        texture.bind()
        // Set wrapping and filtering options
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapping)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapping)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, wrapping)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    }

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

    companion object {
        val brokeTexture: Texture; get() = TextureLoader.getTexture(ResourceKey("broke"))
    }
}