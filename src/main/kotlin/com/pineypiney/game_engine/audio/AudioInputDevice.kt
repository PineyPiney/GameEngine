package com.pineypiney.game_engine.audio

import com.pineypiney.game_engine.GameEngineI
import kool.ByteBuffer
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC10
import org.lwjgl.openal.ALC11
import org.lwjgl.openal.ALUtil
import java.nio.ByteBuffer

class AudioInputDevice(val ptr: Long) {

	constructor(
		specifier: ByteBuffer? = null,
		freq: Int = 44100,
		format: Int = AL10.AL_FORMAT_STEREO16,
		samples: Int = 1024
	) : this(ALC11.alcCaptureOpenDevice(specifier, freq, format, samples))

	constructor(
		specifier: CharSequence?,
		freq: Int = 44100,
		format: Int = AL10.AL_FORMAT_STEREO16,
		samples: Int = 1024
	) : this(ALC11.alcCaptureOpenDevice(specifier, freq, format, samples))

	val name: String get() = ALUtil.getStringList(ptr, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER)?.getOrNull(0) ?: ""
	val error: Int get() = ALC10.alcGetError(ptr)
	val samples: Int get() = ALC10.alcGetInteger(ptr, ALC11.ALC_CAPTURE_SAMPLES)

	fun start() = ALC11.alcCaptureStart(ptr)
	fun stop() = ALC11.alcCaptureStop(ptr)

	fun sample(): ByteBuffer {
		val buffer = ByteBuffer(samples)
		ALC11.alcCaptureSamples(ptr, buffer, samples)
		return buffer
	}

	fun close() {
		if (!ALC11.alcCaptureCloseDevice(ptr)) GameEngineI.warn("Failed to close audio input device $name")
	}

	override fun toString(): String {
		return "AudioDevice $name"
	}

	companion object {
		fun bps(format: Int) = when (format) {
			AL10.AL_FORMAT_STEREO16 -> 4
			AL10.AL_FORMAT_STEREO8,
			AL10.AL_FORMAT_MONO16 -> 2

			else -> 1
		}
	}
}