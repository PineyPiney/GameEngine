package com.pineypiney.game_engine.audio

import com.pineypiney.game_engine.resources.audio.Audio
import org.lwjgl.openal.AL10

class AudioSource(audio: Audio) : AbstractAudioSource() {

	init {
		AL10.alSourcei(ptr, AL10.AL_BUFFER, audio.buf)
	}
}