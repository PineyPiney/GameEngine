package com.pineypiney.game_engine.util.extension_functions

import glm_.and
import glm_.b
import glm_.shr
import kool.lib.map
import kool.toBuffer
import java.nio.ByteBuffer
import java.nio.ShortBuffer

fun ShortBuffer.toByteBuffer(): ByteBuffer{
    return map { listOf((it and 0xff).b, (it shr 8).b) }.flatten().toByteArray().toBuffer()
}