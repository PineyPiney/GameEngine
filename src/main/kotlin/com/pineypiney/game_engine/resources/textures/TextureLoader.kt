package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer

class TextureLoader private constructor() : AbstractResourceLoader<Texture>() {

    private val textures = mutableMapOf<ResourceKey, Texture>()

    fun loadTextures(streams: Map<String, InputStream>) {
        for((fileName, stream) in streams){

            val end = fileName.split('.').getOrNull(1)
            if (!fileTypes.contains(end)) continue

            loadTexture(fileName, stream)

            stream.close()
        }
    }

    private fun loadTexture(name: String, stream: InputStream){
        textures[ResourceKey(name.substringBefore('.'))] =
            loadTexture(name, ResourcesLoader.ioResourceToByteBuffer(stream, 1024))
    }

    private fun loadTexture(name: String, buffer: ByteBuffer): Texture{
        if(!buffer.hasRemaining()){
            GameEngine.logger.warn("Buffer for texture $name is empty")
        }
        else {
            val pointer = loadTextureSettings()

            // Load the image from file
            val details = loadImage(buffer)

            if(details[0] == 0) GameEngine.logger.warn("\nFailed to load texture $name")
            else{
                return Texture(name.substringAfterLast('\\').substringBefore('.'), pointer, details[0], details[1], details[2])
            }
        }

        return Texture.brokeTexture
    }

    private fun loadTextureSettings(wrapping: Int = GL_REPEAT, flip: Boolean = true): Int{
        // Create a handle for the texture
        val texturePointer = glGenTextures()
        // Settings
        loadIndividualSettings(texturePointer, wrapping)
        // Set the flip state of the image (default to true)
        STBImage.stbi_set_flip_vertically_on_load(flip)

        return texturePointer
    }

    private fun loadImage(buffer: ByteBuffer): IntArray {

        // Arrays are Java equivalent for pointers
        val widthA = IntArray(1)
        val heightA = IntArray(1)
        val numChannelsA = IntArray(1)

        var width = 0
        var height = 0
        var numChannels = 0

        // Load texture data from file
        val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, widthA, heightA, numChannelsA, 0)
        if (data != null) {

            width = widthA[0]
            height = heightA[0]
            numChannels = numChannelsA[0]

            val format = when(numChannels) {
                1 -> GL_RED
                3 -> GL_RGB
                4 -> GL_RGBA
                else -> GL_GREEN
            }

            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data)

            glGenerateMipmap(GL_TEXTURE_2D)

            STBImage.stbi_image_free(data)
        }

        buffer.clear()

        return intArrayOf(width, height, numChannels)
    }

    private fun loadIndividualSettings(pointer: Int, wrapping: Int = GL_REPEAT) {

        glBindTexture(GL_TEXTURE_2D, pointer)
        // Set wrapping and filtering options
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapping)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapping)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, wrapping)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    }

    fun getTexture(key: ResourceKey): Texture {
        val t = textures[key]
        return t ?: Texture.brokeTexture
    }

    fun findTexture(name: String): Texture{
        val t = textures[ResourceKey(name)] ?: textures.entries.firstOrNull { (key, _) ->
            key.key.contains(name)
        }?.value
        return t ?: Texture.brokeTexture
    }

    override fun delete() {
        textures.delete()
        textures.clear()
    }

    companion object{
        val fileTypes = arrayOf("png", "jpg", "jpeg", "tga", "bmp", "hdr")
        val INSTANCE = TextureLoader()

        fun getTexture(key: ResourceKey): Texture = INSTANCE.getTexture(key)
        fun findTexture(name: String): Texture = INSTANCE.findTexture(name)

        fun blank(): Texture = INSTANCE.getTexture(ResourceKey("broke"))
    }
}