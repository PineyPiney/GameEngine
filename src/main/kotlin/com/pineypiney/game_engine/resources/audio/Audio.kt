package com.pineypiney.game_engine.resources.audio

import com.pineypiney.game_engine.resources.Resource
import org.lwjgl.openal.AL10

class Audio(val buf: Int) : Resource() {


	val frequency get() = AL10.alGetBufferf(buf, AL10.AL_FREQUENCY)
	val bits get() = AL10.alGetBufferf(buf, AL10.AL_BITS)
	val channels get() = AL10.alGetBufferf(buf, AL10.AL_CHANNELS)
	val size get() = AL10.alGetBufferf(buf, AL10.AL_SIZE)

	override fun delete() {
		AL10.alDeleteBuffers(buf)
	}

	override fun equals(other: Any?): Boolean {
		return other is Audio && buf == other.buf
	}

	override fun hashCode(): Int {
		return buf.hashCode()
	}

	companion object {
		val broke = Audio(0)
	}
}