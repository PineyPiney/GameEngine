package com.pineypiney.game_engine.audio

import glm_.vec3.Vec3
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC11
import org.lwjgl.openal.ALUtil

open class AudioEngine {

    val audio: MutableSet<AudioSource> = mutableSetOf()

    var gain: Float
        get() = AL10.alGetListenerf(AL10.AL_PITCH)
        set(value) = AL10.alListenerf(AL10.AL_PITCH, value)
    var position: Vec3
        get() = getVec3(AL10.AL_POSITION)
        set(value) = setVec3(AL10.AL_POSITION, value)
    var velocity: Vec3
        get() = getVec3(AL10.AL_VELOCITY)
        set(value) = setVec3(AL10.AL_VELOCITY, value)
    var orientation: Vec3
        get() = getVec3(AL10.AL_ORIENTATION)
        set(value) = setVec3(AL10.AL_ORIENTATION, value)

    val allPointers: IntArray; get() = audio.map { it.ptr }.toIntArray()

    fun playAll() = AL10.alSourcePlayv(allPointers)
    fun pauseAll() = AL10.alSourcePausev(allPointers)
    fun stopAll() = AL10.alSourceStopv(allPointers)
    fun rewindAll() = AL10.alSourceRewindv(allPointers)
    fun deleteAll() = AL10.alDeleteBuffers(allPointers)

    open fun updateAudio(){}

    companion object{

        fun getVec3(paramName: Int): Vec3 {
            val array = FloatArray(3)
            AL10.alGetListenerfv(paramName, array)
            return Vec3(0, array)
        }
        fun setVec3(paramName: Int, value: Vec3){
            AL10.alListener3f(paramName, value.x, value.y, value.z)
        }

        fun getAllOutputDevices(): List<String> = ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER) ?: listOf()
        fun getAllInputDevices(): List<String> = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER) ?: listOf()
    }
}