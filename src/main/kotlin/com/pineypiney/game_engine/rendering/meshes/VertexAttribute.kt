package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.maths.normal
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.Vec4ub
import org.lwjgl.opengl.GL11C
import java.nio.ByteBuffer

class VertexAttribute<T, P>(
	val name: String,
	val parent: Parent<P>,
	val size: Int,
	val type: Int,
	val get: (ByteBuffer, Int) -> T,
	val set: (ByteBuffer, Int, T) -> Unit,
	val default: () -> T,
	val toParent: (T) -> P,
	val fromParent: (P) -> T
) {

	val bytes = size * GLFunc.getDataSize(type)

	operator fun component1() = size
	operator fun component2() = type

	fun defaultPair() = Pair(this, default())
	infix fun pair(value: T) = Pair(this, value)

	override fun toString(): String {
		return "$name Attribute"
	}

	class Pair<T, P>(val id: VertexAttribute<T, P>, val value: T) {

        @Suppress("UNCHECKED_CAST")
        @Throws(ClassCastException::class)
		constructor(id: VertexAttribute<T, P>, array: Array<Any>, index: Int) : this(id, array[index] as T)
		operator fun component1() = id
		operator fun component2() = value
		fun set(buffer: ByteBuffer, offset: Int) = id.set(buffer, offset, value)

		fun <N> convert(attrib: VertexAttribute<N, P>): Pair<N, P> {
			val parentValue = id.toParent(value)
			val newValue = attrib.fromParent(parentValue)
			return attrib.pair(newValue)
		}

		override fun toString(): String {
			return "$id($value)"
		}
	}

	companion object {
		val POSITION2D = VertexAttribute<Vec2, Vec3>("Position2D", Parent.POSITION, 2, GL11C.GL_FLOAT, ByteBuffer::getVec2, ByteBuffer::putVec2, ::Vec2, ::Vec3, ::Vec2)
		val POSITION = VertexAttribute("Position", Parent.POSITION, 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3, ::Vec3, ::Vec3, ::Vec3)

		val TEX_COORD = VertexAttribute("TexCoord", Parent.TEXTURE, 2, GL11C.GL_FLOAT, ByteBuffer::getVec2, ByteBuffer::putVec2, ::Vec2, ::Vec3, ::Vec2)
		val TEX_U = VertexAttribute("Tex U", Parent.TEXTURE, 1, GL11C.GL_FLOAT, ByteBuffer::getFloat, ByteBuffer::putFloat, { 0f }, { Vec3(it, 0f, 0f) }, Vec3::x)
		val TEX_V = VertexAttribute("Tex V", Parent.TEXTURE, 1, GL11C.GL_FLOAT, ByteBuffer::getFloat, ByteBuffer::putFloat, { 0f }, { Vec3(0f, it, 0f) }, Vec3::y)

		val COLOUR = VertexAttribute("Colour", Parent.COLOUR, 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4, { Vec4(1f) }, ::Vec4, ::Vec4)
		val COLOUR_BYTES =
			VertexAttribute("Colour Bytes", Parent.COLOUR, 4, GL11C.GL_UNSIGNED_BYTE, ByteBuffer::getVec4ub, ByteBuffer::putVec4ub, { Vec4ub(255) }, { Vec4(it) / 255f }, { Vec4ub(it * 255f) })

		val NORMAL = VertexAttribute("Normal", Parent.NORMAL, 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3, { normal }, ::Vec3, ::Vec3)
		val TANGENT = VertexAttribute("Tangent", Parent.TANGENT, 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3, { Vec3(1f, 0f, 0f) }, ::Vec4, ::Vec3)
		val TANGENT_HANDED = VertexAttribute("Tangent", Parent.TANGENT, 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4, { Vec4(1f, 0f, 0f, 0f) }, ::Vec4, ::Vec4)

		val BONE_IDS = VertexAttribute("BoneIds", Parent.BONE_IDS, 4, GL11C.GL_UNSIGNED_BYTE, ByteBuffer::getVec4ub, ByteBuffer::putVec4ub, { Vec4ub(255) }, ::Vec4i, ::Vec4ub)
		val BONE_WEIGHTS = VertexAttribute("BoneWeights", Parent.BONE_WEIGHTS, 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4, ::Vec4, ::Vec4, ::Vec4)
	}

	class Parent<T>(val value: T) {
		companion object {
			val POSITION = Parent<Vec3>(Vec3())
			val TEXTURE = Parent<Vec3>(Vec3())
			val COLOUR = Parent<Vec4>(Vec4())
			val NORMAL = Parent<Vec3>(Vec3())
			val TANGENT = Parent<Vec4>(Vec4())
			val BONE_IDS = Parent<Vec4i>(Vec4i())
			val BONE_WEIGHTS = Parent<Vec4>(Vec4())
		}
	}
}