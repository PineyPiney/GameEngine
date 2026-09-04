package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.resources.shaders.vulkan.Variable
import com.pineypiney.game_engine.resources.shaders.vulkan.Variables
import glm_.and
import glm_.or
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

abstract class DataType {

	abstract val size: Int
	abstract val manual: Boolean

	abstract fun getUniformMap(name: String, dst: MutableMap<String, String>)

	abstract fun align430(current: Int): Int

	fun getRange(name: String, index: Int): Pair<Int, Int>? {
		val dotI = name.indexOf('.')
		val name0 = if (dotI == -1) name else name.substring(0, dotI)
		val braI = name0.indexOf('[')
		val arrayIndex = if (braI == -1) -1 else name0.substring(braI + 1, name0.lastIndexOf(']')).toIntOrNull() ?: return null

		when (this) {
			is Struct -> {
				val (dataType, offset) = variables[name0] ?: return null
				if (dotI != -1) {
					val (o, s) = dataType.getRange(name.substring(dotI + 1), arrayIndex) ?: return null
					return o + offset to s
				} else {
					return offset to dataType.size
				}
			}

			is Array if index >= 0 -> {
				val arrayOffset = getOffset(index) ?: return null
				val (o, s) = type.getRange(name.substring(dotI + 1), arrayIndex) ?: return null
				return arrayOffset + o to s
			}

			else -> return 0 to size
		}
	}

	class Primitive(val type: GLSLType, override val manual: Boolean) : DataType() {
		override val size: Int get() = type.bytes
		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = type.varName()
		}
		override fun align430(current: Int): Int = current
	}

	class Vec(val type: GLSLType, val length: Int, override val manual: Boolean) : DataType() {

		override val size: Int get() = type.bytes * length
		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = type.symbol + "vec" + length.toString()
		}

		override fun align430(current: Int): Int = getAlignment(current, length)
		override fun toString(): String = "Vec[$type, $length]"

		companion object {
			val regex = Regex("[biud]?vec[234]")
		}
	}

	class Matrix(val type: GLSLType, val rows: Int, val columns: Int, override val manual: Boolean) : DataType() {

		override val size: Int = type.bytes * if (rows == 3) 4 else rows * columns
		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = type.symbol + "mat" + if (rows == columns) rows.toString() else "${columns}x$rows"
		}

		override fun align430(current: Int): Int = getAlignment(current, rows)

		companion object {
			val regex = Regex("d?mat[234](x[234])?")
		}
	}

	class Array(val type: DataType, val typeString: String, val length: Int, override val manual: Boolean) : DataType() {

		val elementSize = type.align430(type.size)
		override val size: Int = elementSize * (length - 1) + type.size

		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = typeString
		}

		override fun align430(current: Int): Int = type.align430(current)

		fun getOffset(index: Int): Int? {
			return if (index !in 0..<length) null
			else elementSize * index
		}
	}

	class Sampler(val type: GLSLType, override val manual: Boolean) : DataType() {

		override val size: Int get() = 4
		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = type.symbol + "sampler2D"
		}
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
		override val manual: Boolean

		constructor(type: GLSLType, qualifiers: Byte, format: String, manual: Boolean) : super() {
			this.type = type
			this.qualifiers = qualifiers
			this.format = format
			this.manual = manual
		}

		constructor(type: GLSLType, params: Map<String, String>, manual: Boolean) : super() {
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
			this.manual = manual
		}

		fun isCoherent() = (qualifiers and 1) > 0
		fun isVolatile() = (qualifiers and 2) > 0
		fun isRestrict() = (qualifiers and 4) > 0
		fun isReadonly() = (qualifiers and 8) > 0
		fun isWriteonly() = (qualifiers and 16) > 0

		override val size: Int get() = 4
		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = type.symbol + "image2D"
		}
		override fun align430(current: Int): Int = current

		companion object {
			val regex = Regex("[iu]?image((1D|2D|2DMS|Cube)(Array)?)|3D|Buffer|2DRect")
		}
	}

	abstract class CustomType(val name: String) : DataType()

	class BufferReference(name: String, val variable: Variable, override val manual: Boolean) : CustomType(name) {

		override val size: Int get() = 8

		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			dst[name] = "int64_t"
		}
		override fun align430(current: Int): Int {
			val i = current % 16
			return when (i) {
				12 -> current + 4
				else -> current
			}
		}
	}

	open class Struct(name: String, val variables: Variables, override val manual: Boolean) : CustomType(name) {

		val min: Int
		val max: Int
		final override val size: Int get() = max - min

		init {
			var min = 255
			var max = 0
			for ((name, type) in variables) {
				min = min(min, type.second)
				max = max(max, type.second + type.first.size)
			}
			this.min = min
			this.max = max
		}

		override fun getUniformMap(name: String, dst: MutableMap<String, String>) {
			for ((varName, variable) in variables) variable.first.getUniformMap(if (name.isEmpty()) varName else "$name.$varName", dst)
		}

		override fun align430(current: Int): Int {
			return variables.values.first().first.align430(current)
		}

		fun getOffsets(): Map<String, Int> {
			return variables.mapValues { (_, v) -> v.second }
		}

		fun getBuffer(buffer: ByteBuffer, offset: Int, name: String): ByteBuffer? {
			val dotI = name.indexOf('.')
			val (dataType, subOffset) = variables[if (dotI == -1) name else name.substring(0, dotI)] ?: return null
			return if (dataType is Struct && dotI != -1) dataType.getBuffer(buffer, offset + subOffset, name.substring(dotI + 1))
			else buffer.slice(offset + subOffset, dataType.size).order(ByteOrder.nativeOrder())
		}
	}

	class PushConstants(name: String, variables: Variables, manual: Boolean) : Struct(name, variables, manual)

	class InterfaceBlock(name: String, variables: Variables, manual: Boolean) : Struct(name, variables, manual)

	companion object {
		fun getAlignment(current: Int, size: Int): Int {
			val i = current % 16
			return when (size) {
				4 -> if (i == 0) current else current + 16 - i
				3 -> if (i == 0 || i == 4) current else current + 16 - i
				2 -> if (i != 12) current else current + 4
				else -> current
			}
		}
	}
}