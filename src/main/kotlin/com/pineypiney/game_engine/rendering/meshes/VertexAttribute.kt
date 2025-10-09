package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.maths.normal
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4ub
import org.lwjgl.opengl.GL11C
import java.nio.ByteBuffer

class VertexAttribute<T>(val name: String, val size: Int, val type: Int, val get: (ByteBuffer, Int) -> T, val set: (ByteBuffer, Int, T) -> Unit, val default: () -> T) {

	val bytes = size * GLFunc.getDataSize(type)

	operator fun component1() = size
	operator fun component2() = type

	fun defaultPair() = Pair(this, default())
	infix fun pair(value: T) = Pair(this, value)

	override fun toString(): String {
		return "$name Attribute"
	}

	class Pair<T>(val id: VertexAttribute<T>, val value: T){
        @Suppress("UNCHECKED_CAST")
        @Throws(ClassCastException::class)
		constructor(id: VertexAttribute<T>, array: Array<Any>, index: Int): this(id, array[index] as T)
		operator fun component1() = id
		operator fun component2() = value
		fun set(buffer: ByteBuffer, offset: Int) = id.set(buffer, offset, value)

		override fun toString(): String {
			return "$id($value)"
		}
	}

	companion object {
		val POSITION2D = VertexAttribute<Vec2>("Position2D", 2, GL11C.GL_FLOAT, ByteBuffer::getVec2, ByteBuffer::putVec2, ::Vec2)
		val POSITION = VertexAttribute<Vec3>("Position", 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3, ::Vec3)
		val TEX_COORD = VertexAttribute<Vec2>("TexCoord", 2, GL11C.GL_FLOAT, ByteBuffer::getVec2, ByteBuffer::putVec2, ::Vec2)
		val COLOUR = VertexAttribute("Colour", 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4, { Vec4(1f) })
		val COLOUR_BYTES = VertexAttribute("Colour Bytes", 4, GL11C.GL_UNSIGNED_BYTE, ByteBuffer::getVec4ub, ByteBuffer::putVec4ub, { Vec4ub(255) })
		val NORMAL = VertexAttribute<Vec3>("Normal", 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3, { normal })
		val TANGENT = VertexAttribute<Vec3>("Tangent", 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3, {Vec3(1f, 0f, 0f)})
		val TANGENT_HANDED = VertexAttribute("Tangent", 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4, {Vec4(1f, 0f, 0f, 0f)})
		val BONE_IDS = VertexAttribute<Vec4ub>("BoneIds", 4, GL11C.GL_UNSIGNED_BYTE, ByteBuffer::getVec4ub, ByteBuffer::putVec4ub, { Vec4ub(255) })
		val BONE_WEIGHTS = VertexAttribute<Vec4>("BoneWeights", 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4, ::Vec4)
	}
}