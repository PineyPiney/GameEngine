package com.pineypiney.game_engine.util.extension_functions

import glm_.and
import glm_.b
import glm_.shr
import kool.map
import kool.toBuffer
import org.lwjgl.PointerBuffer
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer
import java.nio.ByteBuffer
import java.nio.ShortBuffer

fun ShortBuffer.toByteBuffer(): ByteBuffer{
    return map { listOf((it and 0xff).b, (it shr 8).b) }.flatten().toByteArray().toBuffer()
}

inline fun <reified E: Struct<E>> StructBuffer<E, *>.toArray() = Array<E>(capacity()){get()}
fun PointerBuffer.toArray() = LongArray(capacity()){get()}