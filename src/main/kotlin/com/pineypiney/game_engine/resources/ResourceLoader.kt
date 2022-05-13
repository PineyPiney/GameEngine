package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.video.VideoLoader
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.zip.ZipFile

open class ResourceLoader private constructor(){

    fun loadResources(){
        val streamMap: MutableMap<String, InputStream> = mutableMapOf()

        zipFile.entries().asSequence().forEach { entry ->

            val fileName = entry.name
            val stream: InputStream = zipFile.getInputStream(entry)

            streamMap[fileName] = stream
        }

        ShaderLoader.INSTANCE.loadShaders(streamMap.filter { it.key.startsWith("shaders/") })
        TextureLoader.INSTANCE.loadTextures(streamMap.filter { it.key.startsWith("textures/") })
        AudioLoader.INSTANCE.loadAudio(streamMap.filter { it.key.startsWith("audio/") })
        VideoLoader.INSTANCE.loadVideos(streamMap.filter { it.key.startsWith("videos/") })
        ModelLoader.INSTANCE.loadModels(streamMap.filter { it.key.startsWith("models/") })
    }

    fun cleanUp(){
        ShaderLoader.INSTANCE.delete()
        TextureLoader.INSTANCE.delete()
        AudioLoader.INSTANCE.delete()
        VideoLoader.INSTANCE.delete()
        ModelLoader.INSTANCE.delete()
    }

    companion object{

        val INSTANCE: ResourceLoader = ResourceLoader()

        val zipFile = ZipFile("${directory}resources.zip")

        fun getStream(name: String): InputStream = zipFile.getInputStream(zipFile.getEntry(name))

        fun ioResourceToByteBuffer(stream: InputStream, bufferSize: Int): ByteBuffer{

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