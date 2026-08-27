package com.pineypiney.game_engine.util.extension_functions

import glm_.*
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.Vec4ub
import kool.map
import kool.toBuffer
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer
import unsigned.Ushort
import java.nio.BufferOverflowException
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer


fun ByteBuffer.getUshort(i: Int): Ushort {
    val l = this[i + 1].ub.us shl 8
    val s = this[i].ub.i
    return l or s
}

fun ByteBuffer.getVec2(offset: Int = 0): Vec2 = Vec2(getFloat(offset), getFloat(offset + 4))
fun ByteBuffer.getVec2i(offset: Int = 0): Vec2i = Vec2i(getInt(offset), getInt(offset + 4))

fun ByteBuffer.getVec3(offset: Int): Vec3 = Vec3(getFloat(offset), getFloat(offset + 4), getFloat(offset + 8))
fun ByteBuffer.getVec3i(offset: Int = 0): Vec3i = Vec3i(getInt(offset), getInt(offset + 4), getInt(offset + 8))

fun ByteBuffer.getVec4(offset: Int): Vec4 = Vec4(getFloat(offset), getFloat(offset + 4), getFloat(offset + 8), getFloat(offset + 8))
fun ByteBuffer.getVec4i(offset: Int): Vec4i = Vec4i(getInt(offset), getInt(offset + 4), getInt(offset + 8), getInt(offset + 8))
fun ByteBuffer.getVec4ub(offset: Int): Vec4ub = Vec4ub(get(offset), get(offset + 1), get(offset + 2), get(offset + 3))


fun ByteBuffer.put(v: ToBuffer): ByteBuffer {
	if (remaining() < v.size()) throw BufferOverflowException()
	v to this
	position(position() + v.size())
	return this
}

fun ByteBuffer.put(offset: Int, v: ToBuffer): ByteBuffer {
	if (capacity() - offset < v.size()) throw BufferOverflowException()
	v.to(this, offset)
	return this
}

fun IntBuffer.getVec2i(): Vec2i {
	return Vec2i(get(), get())
}

fun IntBuffer.getVec3i(): Vec3i {
	return Vec3i(get(), get(), get())
}

fun ShortBuffer.toByteBuffer(): ByteBuffer {
	return map { listOf((it and 0xff).b, (it shr 8).b) }.flatten().toByteArray().toBuffer()
}

fun ByteBuffer.resize(newCapacity: Int): ByteBuffer {
	return MemoryUtil.memAlloc(newCapacity).put(flip())
}

fun ByteBuffer.resize(newCapacity: Int, stack: MemoryStack): ByteBuffer {
	return stack.malloc(newCapacity).put(flip())
}

inline fun <reified E : Struct<E>> StructBuffer<E, *>.toArray() = Array<E>(capacity()) { get() }
fun PointerBuffer.toArray() = LongArray(capacity()) { get() }