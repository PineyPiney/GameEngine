package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.*
import com.pineypiney.game_engine.util.extension_functions.splitAndTrim
import com.pineypiney.game_engine.util.extension_functions.splitAndTrimWhitespace
import glm_.parseInt

class VulkanShaderBuilder(val stage: ShaderStage) {

	val data = mutableListOf<DataType.CustomType>()
	val defines = mutableMapOf<String, String>()

	val inVariables = mutableMapOf<Int, Variable>()
	val outVariables = mutableMapOf<Int, Variable>()

	val uniforms = mutableListOf<VulkanUBO>()
	var pushConstants: Pair<String, DataType.PushConstants>? = null

	val segments = mutableListOf<GLSLCodeSegment>()

	var i = 0

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

	fun parseDataType(typeName: String, manual: Boolean, map: Map<String, String> = emptyMap()): DataType? {
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
			return DataType.Image(primType, map, manual)
		}

		return null
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

//	fun parseVariable(line: String, start: Int, iterator: ShaderLineIterator): Pair<String, Variables>{
//		val endOfTypeName = (start..<line.length).first { !isName(line[it]) }
//		val typeName = line.substring(start, endOfTypeName)
//		val existingDataType = parseDataType(typeName)
//		if(existingDataType != null){
//			val name = line.substring(endOfTypeName + 1).removeSuffix(";").trim()
//			return name to listOf(existingDataType)
//		}
//		return parseGLSLStruct(iterator)
//	}

	fun parseVulkanLayout(segment: GLSLCodeSegment, manual: Boolean) {
		val (params, str) = parseLayoutQualifiers(segment)
		val parts = str.splitAndTrimWhitespace()
		when (parts[0]) {
			"in" -> {
				val location = params["location"]?.toInt()
				if (location != null && parts.size >= 3) {
					val type = parseDataType(parts[1], manual, params) ?: return
					inVariables[location] = parts[2] to type
				}
			}

			"out" -> {
				val location = params["location"]?.toInt()
				if (location != null && parts.size >= 3) {
					val type = parseDataType(parts[1], manual, params) ?: return
					outVariables[location] = parts[2] to type
				}
			}

			"readonly", "writeonly" -> {
				if (params.containsKey("buffer_reference")) {
					val data = parseGLSLStruct(segment)
					this.data.add(DataType.BufferReference(parts.last(), data.entries.first().let { it.key to it.value.first }, manual))
				}
			}

			"uniform" -> {
				if (params.containsKey("push_constant")) {
					val variables = parseGLSLStruct(segment)
					val nextSegment = segments[i + 1]
					val pushConstantsName = if (nextSegment.code.all(::isName)) {
						i++
						nextSegment.code
					} else ""
					pushConstants = pushConstantsName to DataType.PushConstants(parts[1], variables, manual)
				} else if (params.containsKey("set") && params.containsKey("binding")) {
					if (segment.bracketContents.isNotEmpty()) {
						val uniformBlock = DataType.Struct(parts[1], parseGLSLStruct(segment), manual)
						val nextSegment = segments[i + 1]
						val uniformName = if (nextSegment.code.all(::isName)) {
							i++
							nextSegment.code
						} else ""
						uniforms.add(VulkanUBO(uniformName, params["set"]!!.toInt(), params["binding"]!!.toInt(), uniformBlock))
					} else {
						val type = parseDataType(parts[1], manual, params)
						if (type != null) uniforms.add(VulkanUBO(parts[2], params["set"]!!.toInt(), params["binding"]!!.toInt(), type))
					}
				}
			}
		}
	}

	fun parseUniformsVulkan(moduleName: String, code: String): VulkanShaderData {

		val segmenter = GLSLCodeSegmenter(code)
		segmenter.segmentCode()
		segments.clear()
		segments.addAll(segmenter.currentSegment)
		i = 0

		while (i < segments.size) {

			val segment = segments[i]
			val manual = segment.comment.contains("MANUAL")

			val parts = segment.code.splitAndTrimWhitespace()

			if (parts[0][0] == '#') {
				if (parts[0] == "#define" && parts.size == 3) {
					defines[parts[1]] = parts[2]
				}
			} else if (segment.code.startsWith("const int ")) {
				val ei = segment.code.indexOf('=')
				if (ei != -1) {
					defines[segment.code.substring(10, ei).trim()] = segment.code.substring(ei + 1).trim()
				}
			} else if (parts[0].startsWith("layout")) {
				parseVulkanLayout(segment, manual)
				i++
				continue
			} else when (parts[0]) {
				// Parse Struct
				"struct" -> {
					val structName = parts[1].removeSuffix("{").trim()
					val struct = parseGLSLStruct(segment)
					data.add(DataType.Struct(structName, struct, manual))
				}
			}
			i++
		}

		return VulkanShaderData(moduleName, uniforms, pushConstants)
	}

	companion object {
		fun isName(char: Char) = char.isLetterOrDigit() || char == '_'
	}
}

typealias Variable = Pair<String, DataType>
typealias Variables = Map<String, Pair<DataType, Int>>