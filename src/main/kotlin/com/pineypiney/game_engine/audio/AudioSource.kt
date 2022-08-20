package com.pineypiney.game_engine.audio

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.resources.audio.Audio
import glm_.i
import glm_.vec3.Vec3
import org.lwjgl.openal.AL10

class AudioSource(audio: Audio): Deleteable {

    val ptr: Int = AL10.alGenSources()

    var gain: Float
        get() = AL10.alGetSourcef(ptr, AL10.AL_GAIN)
        set(value) = AL10.alSourcef(ptr, AL10.AL_GAIN, value)
    var pitch: Float
        get() = AL10.alGetSourcef(ptr, AL10.AL_PITCH)
        set(value) = AL10.alSourcef(ptr, AL10.AL_PITCH, value)
    var position: Vec3
        get() = getVec3(AL10.AL_POSITION)
        set(value) = setVec3(AL10.AL_POSITION, value)
    var velocity: Vec3
        get() = getVec3(AL10.AL_VELOCITY)
        set(value) = setVec3(AL10.AL_VELOCITY, value)
    var loop: Boolean
        get() = AL10.alGetSourcei(ptr, AL10.AL_LOOPING) != 0
        set(value) = AL10.alSourcei(ptr, AL10.AL_LOOPING, value.i)
    var type: Int
        get() = AL10.alGetSourcei(ptr, AL10.AL_SOURCE_TYPE)
        set(value) = AL10.alSourcei(ptr, AL10.AL_SOURCE_TYPE, value)
    var minGain: Float
        get() = AL10.alGetSourcef(ptr, AL10.AL_MIN_GAIN)
        set(value) = AL10.alSourcef(ptr, AL10.AL_MIN_GAIN, value)
    var maxGain: Float
        get() = AL10.alGetSourcef(ptr, AL10.AL_MAX_GAIN)
        set(value) = AL10.alSourcef(ptr, AL10.AL_MAX_GAIN, value)

    val state: Int; get() = AL10.alGetSourcei(ptr, AL10.AL_SOURCE_STATE)
    val buffer: Int; get() = AL10.alGetSourcei(ptr, AL10.AL_BUFFER)

    init {
        AL10.alSourcei(ptr, AL10.AL_BUFFER, audio.buf)
    }

    fun play() = AL10.alSourcePlay(ptr)
    fun pause() = AL10.alSourcePause(ptr)
    fun stop() = AL10.alSourceStop(ptr)
    fun rewind() = AL10.alSourceRewind(ptr)

    fun setVec3(paramName: Int, value: Vec3){
        AL10.alSource3f(ptr, paramName, value.x, value.y, value.z)
    }
    fun getVec3(paramName: Int): Vec3 {
        val array = FloatArray(3)
        AL10.alGetSourcefv(ptr, paramName, array)
        return Vec3(0, array)
    }

    override fun delete() {
        AL10.alDeleteSources(ptr)
    }
}