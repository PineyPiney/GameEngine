package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.DeletableResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3i
import kool.toBuffer
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer

class TextureLoader private constructor() : DeletableResourcesLoader<Texture>() {

    override val missing: Texture get() = Texture.broke

    fun loadTextures(streams: Map<String, InputStream>) {
        val pointers = IntArray(streams.size)
        GL11C.glGenTextures(pointers)
        val pList = pointers.toMutableList()
        for((fileName, stream) in streams){
            val p = pList.firstOrNull() ?: GL11C.glGenTextures()
            loadTexture(fileName, stream, p)
            pList.remove(p)
            stream.close()
        }
    }

    private fun loadTexture(name: String, stream: InputStream, pointer: Int){

        map[ResourceKey(name.substringBefore('.'))] =
            loadTexture(name, ResourcesLoader.ioResourceToByteBuffer(stream, 1024), pointer)
    }

    private fun loadTexture(name: String, buffer: ByteBuffer, pointer: Int): Texture{
        if(!buffer.hasRemaining()){
            GameEngineI.logger.warn("Buffer for texture $name is empty")
        }
        else {
            loadIndividualSettings(pointer)

            // Load the image from file
            if(loadImageFromFile(buffer)){
                return Texture(name.substringAfterLast('\\').substringBefore('.'), pointer)
            }

            else {
                GameEngineI.logger.warn("\nFailed to load texture $name")
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


        fun createPointer(wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_NEAREST): Int{
            // Create a handle for the texture
            val texturePointer = glGenTextures()
            // Settings
            loadIndividualSettings(texturePointer, wrapping, filtering)

            return texturePointer
        }

        fun loadIndividualSettings(pointer: Int, wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_LINEAR, target: Int = GL_TEXTURE_2D) {

            glBindTexture(target, pointer)
            // Set wrapping and filtering options
            glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapping)
            glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapping)
            glTexParameteri(target, GL_TEXTURE_WRAP_R, wrapping)
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, filtering)
            glTexParameteri(target, GL_TEXTURE_MAG_FILTER, filtering)
        }

        private fun loadImageFromFile(buffer: ByteBuffer, flip: Boolean = true): Boolean {

            val (data, vec) = loadImageFromMemory(buffer, flip)
            if (data != null) {

                val format = channelsToFormat(vec.z)
                loadImageFromData(data, vec.x, vec.y, format, format, GL_UNSIGNED_BYTE)

                STBImage.stbi_image_free(data)
                return true
            }

            buffer.clear()
            return false
        }

        fun loadImageFromMemory(buffer: ByteBuffer, flip: Boolean = true): Pair<ByteBuffer?, Vec3i>{
            // Arrays are Java equivalent for pointers
            val widthA = IntArray(1)
            val heightA = IntArray(1)
            val numChannelsA = IntArray(1)

            // Set the flip state of the image (default to true)
            STBImage.stbi_set_flip_vertically_on_load(flip)
            // Load texture data from file
            val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, widthA, heightA, numChannelsA, 0)
            return data to Vec3i(widthA[0], heightA[0], numChannelsA[0])
        }

        fun loadImageFromData(data: ByteBuffer?, width: Int, height: Int, format: Int, internalFormat: Int, type: Int){
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, data)

            glGenerateMipmap(GL_TEXTURE_2D)
        }

        fun createTexture(data: ByteArray, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format, type: Int = GL_UNSIGNED_BYTE, wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_NEAREST): Int{
            val pointer = createPointer(wrapping, filtering)
            loadImageFromData(data.toBuffer(), width, height, format, internalFormat, type)
            return pointer
        }

        fun createTexture(data: ByteBuffer?, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format, type: Int = GL_UNSIGNED_BYTE, wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_NEAREST): Int{
            val pointer = createPointer(wrapping, filtering)
            loadImageFromData(data, width, height, format, internalFormat, type)
            return pointer
        }

        fun createAtlas(textures: Iterable<ByteBuffer>, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format, type: Int = GL_UNSIGNED_BYTE): Int {
            val pointer = createTexture(null, width * textures.count(), height, format, internalFormat)
            textures.forEachIndexed { i, data ->
                glTexSubImage2D(GL_TEXTURE_2D, 0, width * i, 0, width, height, format, type, data)
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