package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.maths.normal
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.Vec4ub
import unsigned.Uint
import java.nio.ByteBuffer

class VertexAttribute<T, P>(
	val name: String,
	val parent: Parent<P>,
	val format: TextureFormat,
	val get: (ByteBuffer, Int) -> T,
	val put: (ByteBuffer, Int, T) -> Unit,
	val default: () -> T,
	val fromParent: (P) -> T,
	val toParent: (P, T) -> P
) {

	constructor(
		name: String,
		parent: Parent<P>,
		format: TextureFormat,
		get: (ByteBuffer, Int) -> T,
		set: (ByteBuffer, Int, T) -> Unit,
		default: () -> T,
		toParent: (T) -> P,
		fromParent: (P) -> T,
	) : this(name, parent, format, get, set, default, fromParent, { _, t -> toParent(t) })


	val type: Int get() = format.pixelType.opengl
	val bytes: Int get() = format.pixelSize
	val size: Int get() = bytes / format.pixelType.bytes

	operator fun component1() = size
	operator fun component2() = type

	fun defaultPair() = Pair(this, default())
	infix fun pair(value: T) = Pair(this, value)
	infix fun pairParent(parentValue: P) = Pair(this, fromParent(parentValue))

	override fun toString(): String {
		return "$name Attribute"
	}

	class Pair<T, P>(val id: VertexAttribute<T, P>, val value: T) {

        @Suppress("UNCHECKED_CAST")
        @Throws(ClassCastException::class)
		constructor(id: VertexAttribute<T, P>, array: Array<Any>, index: Int) : this(id, array[index] as T)

		operator fun component1() = id
		operator fun component2() = value
		fun put(buffer: ByteBuffer, offset: Int) = id.put(buffer, offset, value)

		fun convert(rawValue: P): P {
			return id.toParent(rawValue, value)
		}

		override fun toString(): String {
			return "$id($value)"
		}
	}

	companion object {
		val POSITION = VertexAttribute("Position", Parent.POSITION, TextureFormat.RGB32F, ByteBuffer::getVec3, ByteBuffer::put, ::Vec3, ::Vec3, ::Vec3)
		val POSITION2D = VertexAttribute("Position2D", Parent.POSITION, TextureFormat.RG32F, ByteBuffer::getVec2, ByteBuffer::put, ::Vec2, ::Vec2) { p, t -> Vec3(t, p.z) }

		val TEX_COORD = VertexAttribute("TexCoord", Parent.TEXTURE, TextureFormat.RG32F, ByteBuffer::getVec2, ByteBuffer::put, ::Vec2, ::Vec2) { p, t -> Vec3(t, p.z) }
		val TEX_U = VertexAttribute("Tex U", Parent.TEXTURE, TextureFormat.R32F, ByteBuffer::getFloat, ByteBuffer::putFloat, { 0f }, Vec3::x) { p, it -> Vec3(it, p.y, p.z) }
		val TEX_V = VertexAttribute("Tex V", Parent.TEXTURE, TextureFormat.R32F, ByteBuffer::getFloat, ByteBuffer::putFloat, { 0f }, Vec3::y) { p, it -> Vec3(p.x, it, p.z) }

		val COLOUR = VertexAttribute("Colour", Parent.COLOUR, TextureFormat.RGBA32F, ByteBuffer::getVec4, ByteBuffer::put, { Vec4(1f) }, ::Vec4, ::Vec4)
		val COLOUR_BYTES = VertexAttribute("Colour Bytes", Parent.COLOUR, TextureFormat.RGBA8UI, ByteBuffer::getVec4ub, ByteBuffer::put, { Vec4ub(255) }, { Vec4(it) / 255f }, { Vec4ub(it * 255f) })

		val NORMAL = VertexAttribute("Normal", Parent.NORMAL, TextureFormat.RGB32F, ByteBuffer::getVec3, ByteBuffer::put, { normal }, ::Vec3, ::Vec3)
		val TANGENT = VertexAttribute("Tangent", Parent.TANGENT, TextureFormat.RGB32F, ByteBuffer::getVec3, ByteBuffer::put, { Vec3(1f, 0f, 0f) }, ::Vec4, ::Vec3)
		val TANGENT_HANDED = VertexAttribute("Tangent", Parent.TANGENT, TextureFormat.RGBA32F, ByteBuffer::getVec4, ByteBuffer::put, { Vec4(1f, 0f, 0f, 0f) }, ::Vec4, ::Vec4)

		val BONE_IDS = VertexAttribute("Bone Ids", Parent.BONE_IDS, TextureFormat.RGBA32I, ByteBuffer::getVec4i, ByteBuffer::put, { Vec4i(-1) }, ::Vec4i, ::Vec4i)
		val BONE_IDS_UINT = VertexAttribute(
			"Bone Ids Uint",
			Parent.BONE_IDS,
			TextureFormat.R32UI,
			ByteBuffer::getUint,
			ByteBuffer::putUint,
			Uint::MIN,
			{ ui: Uint -> Vec4i(glm.unpackUint4x8(ui)) },
			{ p: Vec4i -> glm.packUint4x8(Vec4ub(p)) })
		val BONE_WEIGHTS = VertexAttribute("Bone Weights", Parent.BONE_WEIGHTS, TextureFormat.RGBA32F, ByteBuffer::getVec4, ByteBuffer::put, ::Vec4, ::Vec4, ::Vec4)
	}

	class Parent<T>(val defaultValue: () -> T) {
		companion object {
			val POSITION = Parent<Vec3>(::Vec3)
			val TEXTURE = Parent<Vec3>(::Vec3)
			val COLOUR = Parent<Vec4>(::Vec4)
			val NORMAL = Parent<Vec3>(::Vec3)
			val TANGENT = Parent<Vec4>(::Vec4)
			val BONE_IDS = Parent<Vec4i>(::Vec4i)
			val BONE_WEIGHTS = Parent<Vec4>(::Vec4)
		}
	}
}