package com.pineypiney.game_engine.util.extension_functions

import glm_.*
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.Vec4ub
import kool.map
import kool.toBuffer
import org.lwjgl.PointerBuffer
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer
import unsigned.Ushort
import java.nio.ByteBuffer
import java.nio.ShortBuffer


fun ByteBuffer.getUshort(i: Int): Ushort {
    val l = this[i + 1].ub.us shl 8
    val s = this[i].ub.i
    return l or s
}

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

fun ByteBuffer.getVec4ub(offset: Int): Vec4ub {
    return Vec4ub(get(offset), get(offset + 1), get(offset + 2), get(offset + 3))
}

fun ByteBuffer.putVec4ub(offset: Int, vec: Vec4ub): ByteBuffer{
    put(offset, vec.x.b).put(offset + 1, vec.y.b).put(offset + 2, vec.z.b).put(offset + 3, vec.w.b)
    return this
}

fun ShortBuffer.toByteBuffer(): ByteBuffer {
	return map { listOf((it and 0xff).b, (it shr 8).b) }.flatten().toByteArray().toBuffer()
}

inline fun <reified E : Struct<E>> StructBuffer<E, *>.toArray() = Array<E>(capacity()) { get() }
fun PointerBuffer.toArray() = LongArray(capacity()) { get() }