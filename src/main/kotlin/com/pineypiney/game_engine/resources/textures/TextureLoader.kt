package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.DeletableResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3i
import kool.toBuffer
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GL32C
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer

class TextureLoader private constructor() : DeletableResourcesLoader<Texture>() {

    override val missing: Texture get() = Texture.broke
    val flags = mutableMapOf<String, Pair<Boolean, Int>>()

    fun loadTextures(streams: Map<String, InputStream>) {
        val pointers = IntArray(streams.size)
        glGenTextures(pointers)
        val pList = pointers.toMutableList()
        for((fileName, stream) in streams){
            val p = pList.firstOrNull() ?: glGenTextures()
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
            GameEngineI.warn("Buffer for texture $name is empty")
        }
        else {
            val (flip, numChan) = flags[name] ?: (true to 0)
            loadIndividualSettings(pointer)

            // Load the image from file
            if(loadImageFromFile(buffer, flip, numChan)){
                return Texture(name.substringAfterLast('\\').substringBefore('.'), pointer)
            }

            else {
                GameEngineI.warn("\nFailed to load texture $name")
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


        fun createPointer(wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_NEAREST, target: Int = GL_TEXTURE_2D): Int{
            // Create a handle for the texture
            val texturePointer = glGenTextures()
            // Settings
            loadIndividualSettings(texturePointer, wrapping, filtering, target)

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

        private fun loadImageFromFile(buffer: ByteBuffer, flip: Boolean = true, numChan: Int = 0): Boolean {

            val (data, vec) = loadImageFromMemory(buffer, flip, numChan)
            if (data != null) {

                val format = channelsToFormat(vec.z)
                loadImageFromData(data, vec.x, vec.y, format, format, GL_UNSIGNED_BYTE)

                STBImage.stbi_image_free(data)
                return true
            }

            buffer.clear()
            return false
        }

        fun loadImageFromMemory(buffer: ByteBuffer, flip: Boolean = true, numChan: Int = 0): Pair<ByteBuffer?, Vec3i>{
            // Arrays are Java equivalent for pointers
            val widthA = IntArray(1)
            val heightA = IntArray(1)
            val numChannelsA = IntArray(1)

            // Set the flip state of the image (default to true)
            STBImage.stbi_set_flip_vertically_on_load(flip)
            // Load texture data from file
            val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, widthA, heightA, numChannelsA, numChan)
            return data to Vec3i(widthA[0], heightA[0], if(numChan == 0) numChannelsA[0] else numChan)
        }

        fun loadImageFromData(data: ByteBuffer?, width: Int, height: Int, format: Int, internalFormat: Int, type: Int){
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, data)

            glGenerateMipmap(GL_TEXTURE_2D)
        }

        fun createTexture(data: ByteArray, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format, type: Int = GL_UNSIGNED_BYTE, wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_NEAREST, target: Int = GL_TEXTURE_2D): Int{
            val pointer = createPointer(wrapping, filtering, target)
            loadImageFromData(data.toBuffer(), width, height, format, internalFormat, type)
            return pointer
        }

        fun createMultisampleTexture(data: ByteArray, width: Int, height: Int, samples: Int = 4, format: Int = GL_RGB, internalFormat: Int = format, type: Int = GL_UNSIGNED_BYTE, target: Int = GL32C.GL_TEXTURE_2D_MULTISAMPLE, fixedSample: Boolean = true): Int{
            val pointer = glGenTextures()
            glBindTexture(target, pointer)
            GL32C.glTexImage2DMultisample(GL32C.GL_TEXTURE_2D_MULTISAMPLE, samples, format, width, height, fixedSample)
            loadImageFromData(data.toBuffer(), width, height, format, internalFormat, type)
            return pointer
        }

        fun createTexture(data: ByteBuffer?, width: Int, height: Int, format: Int = GL_RGB, internalFormat: Int = format, type: Int = GL_UNSIGNED_BYTE, wrapping: Int = GL_CLAMP_TO_EDGE, filtering: Int = GL_NEAREST, target: Int = GL_TEXTURE_2D): Int{
            val pointer = createPointer(wrapping, filtering, target)
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

        fun setFlags(name: String, flip: Boolean, numChannels: Int = 0){
            INSTANCE.flags[name] = flip to numChannels
        }
        fun setFlags(name: String, numChan: Int){
            INSTANCE.flags[name] = true to numChan
        }
    }
}