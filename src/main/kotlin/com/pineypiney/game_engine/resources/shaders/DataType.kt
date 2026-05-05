package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.resources.shaders.vulkan.Variable
import com.pineypiney.game_engine.resources.shaders.vulkan.Variables
import glm_.and
import glm_.or

abstract class DataType {

	abstract val size: Int

	abstract fun align430(current: Int): Int

	class Primitive(val type: GLSLType) : DataType() {
		override val size: Int get() = type.bytes
		override fun align430(current: Int): Int = current
	}

	class Vec(val type: GLSLType, val length: Int) : DataType() {

		override val size: Int get() = type.bytes * length
		override fun align430(current: Int): Int {
			val i = current % 16
			return when (length) {
				4 -> if (i == 0) current else current + 16 - i
				3 -> if (i == 0 || i == 4) current else current + 16 - i
				2 -> if (i != 12) current else current + 4
				else -> current
			}
		}

		override fun toString(): String = "Vec[$type, $length]"

		companion object {
			val regex = Regex("[biud]?vec[234]")
		}
	}

	class Matrix(val type: GLSLType, val rows: Int, columns: Int) : DataType() {

		override val size: Int = type.bytes * if (rows == 3) 4 else rows * columns
		override fun align430(current: Int): Int {
			val i = current % 16
			return when (rows) {
				4 -> if (i == 0) current else current + 16 - i
				3 -> if (i == 0 || i == 4) current else current + 16 - i
				2 -> if (i != 12) current else current + 4
				else -> current
			}
		}

		companion object {
			val regex = Regex("d?mat[234](x[234])?")
		}
	}

	class Sampler(val type: GLSLType) : DataType() {

		override val size: Int get() = 4
		override fun align430(current: Int): Int = current

		companion object {
			val regex = Regex("[iu]?sampler((1D|2D|2DMS|Cube)(Array)?)|3D|Buffer|2DRect")
		}
	}

	// https://wikis.khronos.org/opengl/Image_Load_Store#Image_variables
	class Image : DataType {

		val type: GLSLType
		val qualifiers: Byte
		val format: String

		constructor(type: GLSLType, qualifiers: Byte, format: String) : super() {
			this.type = type
			this.qualifiers = qualifiers
			this.format = format
		}

		constructor(type: GLSLType, params: Map<String, String>) : super() {
			this.type = type
			var qualifiers: Byte = 0
			var format = ""
			for ((key, value) in params) {
				when (key) {
					"coherent" -> qualifiers = qualifiers or 1
					"volatile" -> qualifiers = qualifiers or 2
					"restrict" -> qualifiers = qualifiers or 4
					"readonly" -> qualifiers = qualifiers or 8
					"writeonly" -> qualifiers = qualifiers or 16
					else if (key[0] == 'r') -> format = key
				}
			}
			this.qualifiers = qualifiers
			this.format = format
		}

		fun isCoherent() = (qualifiers and 1) > 0
		fun isVolatile() = (qualifiers and 2) > 0
		fun isRestrict() = (qualifiers and 4) > 0
		fun isReadonly() = (qualifiers and 8) > 0
		fun isWriteonly() = (qualifiers and 16) > 0

		override val size: Int get() = 4
		override fun align430(current: Int): Int = current

		companion object {
			val regex = Regex("[iu]?image((1D|2D|2DMS|Cube)(Array)?)|3D|Buffer|2DRect")
		}
	}

	abstract class CustomType(val name: String) : DataType()

	class BufferReference(name: String, val variable: Variable) : CustomType(name) {

		override val size: Int get() = 8
		override fun align430(current: Int): Int {
			val i = current % 16
			return when (i) {
				12 -> current + 4
				else -> current
			}
		}
	}

	open class Struct(name: String, val variables: Variables) : CustomType(name) {

		val structure: Map<String, Pair<Int, Int>>
		final override val size: Int

		init {
			var i = 0
			var s = 0
			val offsetsMap = mutableMapOf<String, Pair<Int, Int>>()
			for ((name, type) in variables) {
				i = type.align430(i)
				offsetsMap[name] = i to type.size
				i += type.size
				s = i + type.size
			}
			structure = offsetsMap
			this.size = s
		}

		override fun align430(current: Int): Int {
			return variables.values.first().align430(current)
		}

		fun getOffsets(): Map<String, Int> {
			return structure.mapValues { it.value.first }
		}
	}

	class PushConstants(name: String, variables: Variables) : Struct(name, variables)

	class InterfaceBlock(name: String, variables: Variables) : Struct(name, variables)
}