package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.video.VideoLoader
import com.pineypiney.game_engine.util.extension_functions.removeNullValues
import com.pineypiney.game_engine.util.extension_functions.round
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

        GameEngineI.logger.info("Loaded Shaders in ${timeActionM{ ShaderLoader.INSTANCE.loadShaders(streamMap.filter { it.key.startsWith(shaderLocation) }.mapKeys { it.key.removePrefix(shaderLocation) }) }.round(2)} ms")
        GameEngineI.logger.info("Loaded Textures in ${timeActionM{ TextureLoader.INSTANCE.loadTextures(this, streamList.filter { it.startsWith(textureLocation) }.map { it.removePrefix(textureLocation) }) }.round(2)} ms")
        GameEngineI.logger.info("Loaded Audio in ${timeActionM{ AudioLoader.INSTANCE.loadAudio(streamMap.filter { it.key.startsWith(audioLocation) }.mapKeys { it.key.removePrefix(audioLocation) }) }.round(2)} ms")
        GameEngineI.logger.info("Loaded Videos in ${timeActionM{ VideoLoader.INSTANCE.loadVideos(this, streamList.filter { it.startsWith(videoLocation) }.map { it.removePrefix(videoLocation) }) }.round(2)} ms")
        GameEngineI.logger.info("Loaded Models in ${timeActionM{ ModelLoader.INSTANCE.loadModels(streamMap.filter { it.key.startsWith(modelLocation) }.mapKeys { it.key.removePrefix(modelLocation) }) }.round(2)} ms")
    }

    fun cleanUp(){
        ShaderLoader.INSTANCE.delete()
        TextureLoader.INSTANCE.delete()
        AudioLoader.INSTANCE.delete()
        VideoLoader.INSTANCE.delete()
        ModelLoader.INSTANCE.delete()
    }

    companion object{

        fun lowercaseExtension(file: String): String = file.split('.').run { this[0] + '.' + this[1].lowercase() }

        fun timeAction(action: () -> Unit): Long{
            val start = System.nanoTime()
            action()
            return System.nanoTime() - start
        }

        fun timeActionM(action: () -> Unit): Double{
            return timeAction(action) / 1e6
        }

        fun ioResourceToByteBuffer(stream: InputStream, bufferSize: Int = 1024, resize: Boolean = true): ByteBuffer {

            val rbc: ReadableByteChannel = Channels.newChannel(stream)
            var buffer: ByteBuffer = BufferUtils.createByteBuffer(bufferSize)

            while (true) {
                val bytes = rbc.read(buffer)
                if (bytes == -1 || !resize) {
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