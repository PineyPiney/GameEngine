package com.pineypiney.game_engine.audio

import org.lwjgl.openal.ALC10
import java.nio.ByteBuffer
import java.nio.CharBuffer

class AudioDevice(val ptr: Long, attrList: IntArray? = null) {

    constructor(specifier: ByteBuffer? = null, attrList: IntArray? = null): this(ALC10.alcOpenDevice(specifier), attrList)
    constructor(specifier: CharBuffer, attrList: IntArray? = null): this(ALC10.alcOpenDevice(specifier), attrList)

    val context = ALC10.alcCreateContext(ptr, attrList)

    init {
        ALC10.alcMakeContextCurrent(context)
    }

    fun close(){
        ALC10.alcCloseDevice(ptr)
        ALC10.alcDestroyContext(context)
    }
}