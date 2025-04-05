package com.pineypiney.game_engine.util.extension_functions

import glm_.and
import glm_.b
import glm_.shr
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import kool.map
import kool.toBuffer
import org.lwjgl.PointerBuffer
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer
import java.nio.ByteBuffer
import java.nio.ShortBuffer

fun ByteBuffer.getVec2(offset: Int): Vec2{
	return Vec2(getFloat(offset), getFloat(offset + 4))
}

fun ByteBuffer.putVec2(offset: Int, vec: Vec2): ByteBuffer{
	putFloat(offset, vec.x).putFloat(offset + 4, vec.y)
	return this
}

fun ByteBuffer.getVec3(offset: Int): Vec3{
	return Vec3(getFloat(offset), getFloat(offset + 4), getFloat(offset + 8))
}

fun ByteBuffer.putVec3(offset: Int, vec: Vec3): ByteBuffer{
	putFloat(offset, vec.x).putFloat(offset + 4, vec.y).putFloat(offset + 8, vec.z)
	return this
}

fun ByteBuffer.getVec4(offset: Int): Vec4{
	return Vec4(getFloat(offset), getFloat(offset + 4), getFloat(offset + 8), getFloat(offset + 8))
}

fun ByteBuffer.putVec4(offset: Int, vec: Vec4): ByteBuffer{
	putFloat(offset, vec.x).putFloat(offset + 4, vec.y).putFloat(offset + 8, vec.z).putFloat(offset + 12, vec.w)
	return this
}

fun ByteBuffer.getVec4i(offset: Int): Vec4i{
	return Vec4i(getInt(offset), getInt(offset + 4), getInt(offset + 8), getInt(offset + 8))
}

fun ByteBuffer.putVec4i(offset: Int, vec: Vec4i): ByteBuffer{
	putInt(offset, vec.x).putInt(offset + 4, vec.y).putInt(offset + 8, vec.z).putInt(offset + 12, vec.w)
	return this
}

fun ShortBuffer.toByteBuffer(): ByteBuffer {
	return map { listOf((it and 0xff).b, (it shr 8).b) }.flatten().toByteArray().toBuffer()
}

inline fun <reified E : Struct<E>> StructBuffer<E, *>.toArray() = Array<E>(capacity()) { get() }
fun PointerBuffer.toArray() = LongArray(capacity()) { get() }