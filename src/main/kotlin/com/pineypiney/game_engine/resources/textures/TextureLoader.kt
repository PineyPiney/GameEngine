package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2i
import kool.toBuffer
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer

class TextureLoader private constructor() : AbstractResourceLoader<Texture>() {

    override val missing: Texture = Texture.missing

    fun loadTextures(streams: Map<String, InputStream>) {
        for((fileName, stream) in streams){

            val end = fileName.split('.').getOrNull(1)
            if (!fileTypes.contains(end)) continue

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
            val details = loadImageFromFile(buffer)

            if(details[0] == 0) GameEngine.logger.warn("\nFailed to load texture $name")
            else{
                return Texture(name.substringAfterLast('\\').substringBefore('.'), pointer, details.x, details.y)
            }
        }

        return Texture.brokeTexture
    }

    fun findTexture(name: String): Texture{
        val t = map[ResourceKey(name)] ?: map.entries.firstOrNull { (key, _) ->
            key.key.contains(name)
        }?.value
        return t ?: Texture.brokeTexture
    }

    companion object{
        val fileTypes = arrayOf("png", "jpg", "jpeg", "tga", "bmp", "hdr")
        val INSTANCE = TextureLoader()

        operator fun get(key: ResourceKey) = INSTANCE[key]
        fun getTexture(key: ResourceKey): Texture = INSTANCE[(key)]
        fun findTexture(name: String): Texture = INSTANCE.findTexture(name)


        fun createPointer(wrapping: Int = GL_REPEAT, flip: Boolean = true): Int{
            // Create a handle for the texture
            val texturePointer = glGenTextures()
            // Settings
            loadIndividualSettings(texturePointer, wrapping)
            // Set the flip state of the image (default to true)
            STBImage.stbi_set_flip_vertically_on_load(flip)

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

        private fun loadImageFromFile(buffer: ByteBuffer): Vec2i {

            // Arrays are Java equivalent for pointers
            val widthA = IntArray(1)
            val heightA = IntArray(1)
            val numChannelsA = IntArray(1)

            var width = 0
            var height = 0

            // Load texture data from file
            val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, widthA, heightA, numChannelsA, 0)
            if (data != null) {

                width = widthA[0]
                height = heightA[0]

                val format = when(numChannelsA[0]) {
                    1 -> GL_RED
                    3 -> GL_RGB
                    4 -> GL_RGBA
                    else -> GL_GREEN
                }

                loadImageFromData(data, width, height, format)
            }

            buffer.clear()

            return Vec2i(width, height)
        }

        fun loadImageFromData(data: ByteBuffer, width: Int, height: Int, format: Int){
            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data)

            glGenerateMipmap(GL_TEXTURE_2D)

            STBImage.stbi_image_free(data)
        }

        fun createTexture(data: ByteArray, width: Int, height: Int, format: Int = GL_RGB): Int{
            val pointer = createPointer()
            loadImageFromData(data.toBuffer(), width, height, format)
            return pointer
        }

        val formatToChannels = mapOf(
            GL_RED to 1,
            GL_GREEN to 1,
            GL_BLUE to 1,
            GL_ALPHA to 1,
            GL_RGB to 3,
            GL_RGBA to 4
        )
    }
}