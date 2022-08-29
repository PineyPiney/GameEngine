package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.ResourceKey
import kool.toBuffer
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer

class TextureLoader private constructor() : AbstractResourceLoader<Texture>() {

    override val missing: Texture get() = Texture.broke

    fun loadTextures(loader: ResourcesLoader, textures: List<String>) {
        for(fileName in textures){

            val end = fileName.split('.').getOrNull(1)
            if (!fileTypes.contains(end)) continue

            val stream  = loader.getStream("textures/$fileName") ?: continue
            loadTexture(fileName, stream)
            stream.close()
        }
    }

    private fun loadTexture(name: String, stream: InputStream){

        map[ResourceKey(name.substringBefore('.'))] =
            loadTexture(name, ResourcesLoader.ioResourceToByteBuffer(stream, 1024))
    }

    private fun loadTexture(name: String, buffer: ByteBuffer): Texture{
        if(!buffer.hasRemaining()){
            GameEngine.logger.warn("Buffer for texture $name is empty")
        }
        else {
            val pointer = createPointer()

            // Load the image from file
            loadImageFromFile(buffer)

            if(loadImageFromFile(buffer)){
                return Texture(name.substringAfterLast('\\').substringBefore('.'), pointer)
            }

            else {
                GameEngine.logger.warn("\nFailed to load texture $name")
            }
        }

        return Texture.broke
    }

    fun findTexture(name: String): Texture{
        val t = map[ResourceKey(name)] ?: map.entries.firstOrNull { (key, _) ->
            key.key.contains(name)
        }?.value
        return t ?: Texture.broke
    }

    companion object{
        val fileTypes = arrayOf("png", "jpg", "jpeg", "tga", "bmp", "hdr")
        val INSTANCE = TextureLoader()

        operator fun get(key: ResourceKey) = INSTANCE[key]
        fun getTexture(key: ResourceKey): Texture = INSTANCE[key]
        fun findTexture(name: String): Texture = INSTANCE.findTexture(name)


        fun createPointer(wrapping: Int = GL_REPEAT): Int{
            // Create a handle for the texture
            val texturePointer = glGenTextures()
            // Settings
            loadIndividualSettings(texturePointer, wrapping)

            return texturePointer
        }

        fun loadIndividualSettings(pointer: Int, wrapping: Int = GL_REPEAT) {

            glBindTexture(GL_TEXTURE_2D, pointer)
            // Set wrapping and filtering options
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapping)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapping)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, wrapping)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        }

        private fun loadImageFromFile(buffer: ByteBuffer, flip: Boolean = true): Boolean {

            // Arrays are Java equivalent for pointers
            val widthA = IntArray(1)
            val heightA = IntArray(1)
            val numChannelsA = IntArray(1)

            // Set the flip state of the image (default to true)
            STBImage.stbi_set_flip_vertically_on_load(flip)
            // Load texture data from file
            val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, widthA, heightA, numChannelsA, 0)
            if (data != null) {

                val width = widthA[0]
                val height = heightA[0]

                val format = channelsToFormat(numChannelsA[0])

                loadImageFromData(data, width, height, format, format)

                STBImage.stbi_image_free(data)
                return true
            }

            buffer.clear()
            return false
        }

        fun loadImageFromData(data: ByteBuffer?, width: Int, height: Int, format: Int, internalFormat: Int){
            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, internalFormat, GL_UNSIGNED_BYTE, data)

            glGenerateMipmap(GL_TEXTURE_2D)
        }

        fun createTexture(data: ByteArray, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format): Int{
            val pointer = createPointer()
            loadImageFromData(data.toBuffer(), width, height, format, internalFormat)
            return pointer
        }

        fun createTexture(data: ByteBuffer?, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format): Int{
            val pointer = createPointer()
            loadImageFromData(data, width, height, format, internalFormat)
            return pointer
        }

        fun createAtlas(textures: Iterable<ByteBuffer>, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format): Int {
            val pointer = createTexture(null, width * textures.count(), height, format, internalFormat)
            textures.forEachIndexed { i, data ->
                glTexSubImage2D(GL_TEXTURE_2D, 0, width * i, 0, width, height, format, GL_UNSIGNED_BYTE, data)
            }
            return pointer
        }

        fun formatToChannels(format: Int?) = when(format) {
            GL_RGB,
            GL_BGR -> 3
            GL_RGBA,
            GL_BGRA-> 4
            GL_RED,
            GL_GREEN,
            GL_BLUE,
            GL_ALPHA -> 1
            else -> 3
        }

        fun channelsToFormat(channels: Int?) = when(channels){
            4 -> GL_RGBA
            3 -> GL_RGB
            1 -> GL_RED
            else -> GL_RGB
        }
    }
}