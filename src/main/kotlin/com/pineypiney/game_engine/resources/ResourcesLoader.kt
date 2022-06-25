package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.video.VideoLoader
import com.pineypiney.game_engine.util.extension_functions.removeNullValues
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

abstract class ResourcesLoader {

    open val shaderLocation = "shaders/"
    open val textureLocation = "textures/"
    open val fontLocation = "textures/fonts/"
    open val audioLocation = "audio/"
    open val videoLocation = "videos/"
    open val modelLocation = "models/"

    abstract val streamList: Set<String>

    abstract fun getStream(name: String): InputStream?

    fun getStreams(): Map<String, InputStream> {
        return streamList.associateWith { entry -> getStream(entry) }.removeNullValues()
    }

    fun loadResources(){

        val streamMap = getStreams()

        ShaderLoader.INSTANCE.loadShaders(streamMap.filter { it.key.startsWith(shaderLocation) }.mapKeys { it.key.removePrefix(shaderLocation) })
        TextureLoader.INSTANCE.loadTexture(streamMap.filter { it.key.startsWith(textureLocation) }.mapKeys { it.key.removePrefix(textureLocation) })
        AudioLoader.INSTANCE.loadAudio(streamMap.filter { it.key.startsWith(audioLocation) }.mapKeys { it.key.removePrefix(audioLocation) })
        VideoLoader.INSTANCE.loadVideos(streamMap.filter { it.key.startsWith(videoLocation) }.mapKeys { it.key.removePrefix(videoLocation) })
        ModelLoader.INSTANCE.loadModels(streamMap.filter { it.key.startsWith(modelLocation) }.mapKeys { it.key.removePrefix(modelLocation) })
    }

    fun cleanUp(){
        ShaderLoader.INSTANCE.delete()
        TextureLoader.INSTANCE.delete()
        AudioLoader.INSTANCE.delete()
        VideoLoader.INSTANCE.delete()
        ModelLoader.INSTANCE.delete()
    }

    companion object{

        fun ioResourceToByteBuffer(stream: InputStream, bufferSize: Int): ByteBuffer {

            val rbc: ReadableByteChannel = Channels.newChannel(stream)
            var buffer: ByteBuffer = BufferUtils.createByteBuffer(bufferSize)

            while (true) {
                val bytes = rbc.read(buffer)
                if (bytes == -1) {
                    break
                }
                if (buffer.remaining() == 0) {
                    buffer = resizeBuffer(buffer, buffer.capacity() * 2)
                }
            }

            buffer.flip()
            return buffer
        }

        private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
            val newBuffer = BufferUtils.createByteBuffer(newCapacity)
            buffer.flip()
            newBuffer.put(buffer)
            return newBuffer
        }
    }
}