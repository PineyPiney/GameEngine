package com.pineypiney.game_engine.resources.audio

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import java.io.BufferedInputStream
import java.io.InputStream
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class AudioLoader private constructor(): AbstractResourceLoader<Audio>() {

    private val audioMap: MutableMap<ResourceKey, Audio> = mutableMapOf()

    fun loadAudio(streams: Map<String, InputStream>) {
        streams.forEach { (fileName, stream) ->

            val i = fileName.lastIndexOf(".")
            if (i <= 0) return@forEach
            val type = fileName.substring(i + 1)

            loadAudio(fileName.removeSuffix(".$type"), AudioSystem.getAudioInputStream(BufferedInputStream(stream)))

            stream.close()
        }
    }

    private fun loadAudio(name: String, stream: AudioInputStream){
        audioMap[ResourceKey(name)] = Audio(stream)
    }

    fun getAudio(key: ResourceKey): Audio {
        val a = audioMap[key]
        return a ?: Audio.brokeAudio
    }

    override fun delete() {
        audioMap.delete()
        audioMap.clear()
    }

    companion object{
        val INSTANCE: AudioLoader =
            AudioLoader()
    }
}