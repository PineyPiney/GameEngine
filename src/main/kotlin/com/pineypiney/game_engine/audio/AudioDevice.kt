package com.pineypiney.game_engine.audio

import com.pineypiney.game_engine.GameEngineI
import org.lwjgl.openal.ALC10
import org.lwjgl.openal.ALC11
import org.lwjgl.openal.ALUtil
import java.nio.ByteBuffer

class AudioDevice(val ptr: Long, attrList: IntArray? = null) {

    constructor(specifier: ByteBuffer? = null, attrList: IntArray? = null): this(ALC10.alcOpenDevice(specifier), attrList)
    constructor(specifier: CharSequence?, attrList: IntArray? = null): this(ALC10.alcOpenDevice(specifier), attrList)

    val context = ALC10.alcCreateContext(ptr, attrList)

    val name: String get() = ALUtil.getStringList(ptr, ALC11.ALC_ALL_DEVICES_SPECIFIER)?.getOrNull(0) ?: ""
    val error: Int get() = ALC10.alcGetError(ptr)

    init {
        ALC10.alcMakeContextCurrent(context)
    }

    fun close(){
        error
        ALC10.alcDestroyContext(context)
        val e = error
        if(e != 0) GameEngineI.logger.warn("Error after destroying context for audio device $name: $e")
        if(!ALC10.alcCloseDevice(ptr)) GameEngineI.logger.warn("Failed to close audio device $name")
    }

    override fun toString(): String {
        return "AudioDevice $name"
    }
}