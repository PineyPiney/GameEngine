package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.*
import com.pineypiney.game_engine.util.extension_functions.splitAndTrim
import com.pineypiney.game_engine.util.extension_functions.splitAndTrimWhitespace

class VulkanShaderBuilder(val stage: ShaderStage) {

	val data = mutableListOf<DataType.CustomType>()

	val inVariables = mutableMapOf<Int, Variable>()
	val outVariables = mutableMapOf<Int, Variable>()

	val uniforms = mutableMapOf<String, VulkanUBO>()
	var pushConstants: Pair<String, DataType.PushConstants>? = null

	val segments = mutableListOf<GLSLCodeSegment>()

	var i = 0

	fun parseDataType(typeName: String, map: Map<String, String> = emptyMap()): DataType? {
		data.firstOrNull { it.name == typeName }?.let { return it }
		val primitives = GLSLType.entries
		val primitive = primitives.firstOrNull { it.name.equals(typeName, true) }
		if (primitive != null) return DataType.Primitive(primitive)

		if (DataType.Vec.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 'v') GLSLType.FLOAT
			else primitives.first { it.name[0].lowercaseChar() == primChar }
			val size = typeName.last().digitToInt()
			return DataType.Vec(primType, size)
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
			return DataType.Matrix(primType, rows, columns)
		}

		if (DataType.Sampler.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 's') GLSLType.FLOAT
			else primitives.first { it.name[0].lowercaseChar() == primChar }
			return DataType.Sampler(primType)
		}

		if (DataType.Image.regex.matches(typeName)) {
			val primChar = typeName[0]
			val primType = if (primChar == 'i') GLSLType.FLOAT
			else primitives.first { it.name[0].lowercaseChar() == primChar }
			return DataType.Image(primType, map)
		}

		return null
	}

	fun parseGLSLStruct(segment: GLSLCodeSegment): Variables {
		val struct = mutableMapOf<String, DataType>()
		for (subsegment in segment.bracketContents) {
			val parts = subsegment.code.splitAndTrimWhitespace()
			if (parts.size == 2) {
				val key = parts[1]
				val type = parseDataType(parts[0])
				if (type != null) struct[key] = type
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

	fun parseVulkanLayout(segment: GLSLCodeSegment) {
		val openBracket = segment.code.indexOf('(')
		val endBracket = segment.code.lastIndexOf(')')
		val params = segment.code.substring(openBracket + 1, endBracket).split(',').associate { it.splitAndTrim('=').run { if (size == 2) get(0) to get(1) else get(0) to get(0) } }
		val parts = segment.code.substring(endBracket + 1).splitAndTrimWhitespace()
		when (parts[0]) {
			"in" -> {
				val location = params["location"]?.toInt()
				if (location != null && parts.size >= 3) {
					val type = parseDataType(parts[1], params) ?: return
					inVariables[location] = parts[2] to type
				}
			}

			"out" -> {
				val location = params["location"]?.toInt()
				if (location != null && parts.size >= 3) {
					val type = parseDataType(parts[1], params) ?: return
					outVariables[location] = parts[2] to type
				}
			}

			"readonly", "writeonly" -> {
				if (params.containsKey("buffer_reference")) {
					val data = parseGLSLStruct(segment)
					this.data.add(DataType.BufferReference(parts.last(), data.entries.first().toPair()))
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
					pushConstants = pushConstantsName to DataType.PushConstants(parts[1], variables)
				} else if (params.containsKey("set") && params.containsKey("binding")) {
					val type = parseDataType(parts[1], params)
					if (type != null) uniforms[parts[2]] = VulkanUBO(params["set"]!!.toInt(), params["binding"]!!.toInt(), type)
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
			if (segment.comment.contains("MANUAL")) {
				i++
				continue
			}

			val parts = segment.code.splitAndTrimWhitespace()

			if (parts[0].startsWith("layout")) {
				parseVulkanLayout(segment)
				i++
				continue
			}

			when (parts[0]) {
				// Parse Struct
				"struct" -> {
					val structName = parts[1].removeSuffix("{").trim()
					val struct = parseGLSLStruct(segment)
					data.add(DataType.Struct(structName, struct))
				}
			}
			i++
		}

		return VulkanShaderData(moduleName, data, uniforms, pushConstants)
	}

	companion object {
		fun isName(char: Char) = char.isLetterOrDigit() || char == '_'
	}
}

typealias Variable = Pair<String, DataType>
typealias Variables = Map<String, DataType>