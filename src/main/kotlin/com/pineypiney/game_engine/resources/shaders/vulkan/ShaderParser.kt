package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.GLSLCodeSegment
import com.pineypiney.game_engine.resources.shaders.GLSLCodeSegmenter
import com.pineypiney.game_engine.resources.shaders.GLSLType
import com.pineypiney.game_engine.util.extension_functions.splitAndTrim
import com.pineypiney.game_engine.util.extension_functions.splitAndTrimWhitespace
import glm_.parseInt

abstract class ShaderParser {

	val segments = mutableListOf<GLSLCodeSegment>()
	var i = 0

	val defines = mutableMapOf<String, String>()
	val data = mutableListOf<DataType.CustomType>()

	val inVariables = mutableMapOf<Int, Variable>()
	val outVariables = mutableMapOf<Int, Variable>()

	fun parseInteger(str: String): Int {
		return parseInteger(
			defines[str] ?: return try {
				str.toInt()
			} catch (e: Exception) {
				1
			}
		)
	}

	fun parseLayoutQualifiers(segment: GLSLCodeSegment): Pair<Map<String, String>, String> {
		val openBracket = segment.code.indexOf('(')
		val endBracket = segment.code.lastIndexOf(')')
		val params = segment.code.substring(openBracket + 1, endBracket).split(',').associate { it.splitAndTrim('=').run { if (size == 2) get(0) to get(1) else get(0) to get(0) } }
		return params to segment.code.substring(endBracket + 1)
	}

	fun parseGLSLStruct(segment: GLSLCodeSegment): Variables {
		val struct = mutableMapOf<String, Pair<DataType, Int>>()
		var offset = 0
		for (subsegment in segment.bracketContents) {
			val parts: List<String> = if (subsegment.code.startsWith("layout(")) {
				val (params, str) = parseLayoutQualifiers(subsegment)
				params["offset"]?.let { offset = it.parseInt() }
				str.splitAndTrimWhitespace()
			} else subsegment.code.splitAndTrimWhitespace()

			if (parts.size == 2) {
				val key = parts[1]
				val type = parseDataType(parts[0], segment.comment.contains("MANUAL"))
				if (type != null) {
					offset = type.align430(offset)
					val bi = key.indexOf('[')
					if (bi != -1) {
						val arraySize = parseInteger(key.substring(bi + 1, key.length - 1))
						val array = DataType.Array(type, parts[0], arraySize, type.manual)
						struct[key.substring(0, bi) + "[0]"] = array to offset
						offset += array.size
					} else {
						struct[key] = type to offset
						offset += type.size
					}
				}
			}
		}
		return struct
	}

	fun parseDataType(typeName: String, manual: Boolean, params: Map<String, String> = emptyMap()): DataType? {
		data.firstOrNull { it.name == typeName }?.let { return it }
		val primitives = GLSLType.entries
		val primitive = primitives.firstOrNull { it.name.equals(typeName, true) }
		if (primitive != null) return DataType.Primitive(primitive, manual)

		if (DataType.Vec.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 'v') GLSLType.FLOAT
			else primitives.first { it.name[0].lowercaseChar() == primChar }
			val size = typeName.last().digitToInt()
			return DataType.Vec(primType, size, manual)
		}

		if (DataType.Matrix.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 'd') GLSLType.DOUBLE
			else GLSLType.FLOAT
			val xI = typeName.indexOf('x')
			val rows: Int
			val columns: Int
			if (xI == -1) {
				rows = typeName.last().digitToInt()
				columns = rows
			} else {
				rows = typeName[xI + 1].digitToInt()
				columns = typeName[xI - 1].digitToInt()
			}
			return DataType.Matrix(primType, rows, columns, manual)
		}

		if (DataType.Sampler.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 's') GLSLType.FLOAT
			else primitives.first { it.name[0].lowercaseChar() == primChar }
			return DataType.Sampler(primType, manual)
		}

		if (DataType.Image.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 'i') GLSLType.FLOAT
			else primitives.first { it.name[0].lowercaseChar() == primChar }
			return DataType.Image(primType, params, manual)
		}

		return null
	}

	abstract fun parseLayout(segment: GLSLCodeSegment, params: Map<String, String>, parts: List<String>, manual: Boolean)
	abstract fun platformParse(segment: GLSLCodeSegment, parts: List<String>, manual: Boolean)

	fun parse(code: String) {
		val segmenter = GLSLCodeSegmenter(code)
		segmenter.segmentCode()
		segments.clear()
		segments.addAll(segmenter.currentSegment)
		i = 0
		while (i < segmenter.currentSegment.size) {
			val segment = segmenter.currentSegment[i]
			val manual = segment.comment.contains("MANUAL")

			val parts = segment.code.splitAndTrimWhitespace()

			// #defines
			if (parts[0][0] == '#') {
				if (parts[0] == "#define" && parts.size == 3) {
					defines[parts[1]] = parts[2]
				}
			}
			// const ints
			else if (segment.code.startsWith("const int ")) {
				val ei = segment.code.indexOf('=')
				if (ei != -1) {
					defines[segment.code.substring(10, ei).trim()] = segment.code.substring(ei + 1).trim()
				}
			} else if (parts[0].startsWith("layout")) {
				val (params, str) = parseLayoutQualifiers(segment)
				val parts = str.splitAndTrimWhitespace()
				parseLayout(segment, params, parts, manual)
			} else platformParse(segment, parts, manual)
			i++
		}
	}

	companion object {
		fun isName(char: Char) = char.isLetterOrDigit() || char == '_'
	}
}

typealias Variable = Pair<String, DataType>
typealias Variables = Map<String, Pair<DataType, Int>>