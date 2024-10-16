package com.pineypiney.game_engine.resources.audio

import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.i
import kool.ByteBuffer
import org.lwjgl.openal.AL10
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

open class AudioLoader protected constructor() {

	val map: MutableMap<ResourceKey, Triple<ByteBuffer, Int, Int>> = mutableMapOf()
	val missing: Triple<ByteBuffer, Int, Int> get() = Triple(ByteBuffer(0), 1, 1)

	fun loadAudio(streams: Map<String, InputStream>) {
		for ((fileName, stream) in streams) {
			1 to 2
			val i = fileName.lastIndexOf(".")
			if (i <= 0) continue
			val type = fileName.substring(i + 1)

			val name = fileName.removeSuffix(".$type")
			val aStream = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
			//loadAudio(name, aStream)
			loadAudio(name, aStream)

			aStream.close()
			stream.close()
		}
	}

	private fun loadAudio(name: String, stream: AudioInputStream) {
		val f = stream.format
		val format = when (f.channels + f.sampleSizeInBits) {
			9 -> AL10.AL_FORMAT_MONO8
			10 -> AL10.AL_FORMAT_STEREO8
			17 -> AL10.AL_FORMAT_MONO16
			else -> AL10.AL_FORMAT_STEREO16
		}

		val buffer = ResourcesLoader.ioResourceToByteBuffer(stream, stream.frameLength.i * f.frameSize, false)
		map[ResourceKey(name)] = Triple(buffer, format, f.sampleRate.i)
	}

	operator fun get(key: ResourceKey): Audio = bufferAudio(map[key] ?: missing)

	fun delete() = map.clear()

	companion object {
		val INSTANCE: AudioLoader = AudioLoader()

		operator fun get(key: ResourceKey): Audio = INSTANCE[key]

		fun bufferAudio(buffer: ByteBuffer, format: Int, sampleRate: Int): Audio {
			val buf = AL10.alGenBuffers()
			AL10.alBufferData(buf, format, buffer, sampleRate)
			return Audio(buf)
		}

		fun bufferAudio(data: Triple<ByteBuffer, Int, Int>) = bufferAudio(data.first, data.second, data.third)
	}
}