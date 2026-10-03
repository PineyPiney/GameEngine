package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.GLSLCodeSegment
import com.pineypiney.game_engine.resources.shaders.ShaderStage

class VulkanShaderBuilder(val stage: ShaderStage) : ShaderParser() {

	val uniforms = mutableListOf<VulkanBufferObject>()
	val storages = mutableListOf<VulkanBufferObject>()
	var pushConstants: Pair<String, DataType.PushConstants>? = null

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

	override fun parseLayout(segment: GLSLCodeSegment, params: Map<String, String>, parts: List<String>, manual: Boolean) {
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
				} else if (params.containsKey("set") && params.containsKey("binding")) {
					val storageBlock = DataType.Struct(parts[1], parseGLSLStruct(segment), manual)
					val nextSegment = segments[i + 1]
					val storageName = if (nextSegment.code.all(::isName)) {
						i++
						nextSegment.code
					} else parts.last()
					storages.add(VulkanBufferObject(storageName, params["set"]!!.toInt(), params["binding"]!!.toInt(), storageBlock))
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
						uniforms.add(VulkanBufferObject(uniformName, params["set"]!!.toInt(), params["binding"]!!.toInt(), uniformBlock))
					} else {
						val type = parseDataType(parts[1], manual, params)
						if (type != null) uniforms.add(VulkanBufferObject(parts[2], params["set"]!!.toInt(), params["binding"]!!.toInt(), type))
					}
				}
			}
		}
	}

	override fun platformParse(segment: GLSLCodeSegment, parts: List<String>, manual: Boolean) {
		when (parts[0]) {
			// Parse Struct
			"struct" -> {
				val structName = parts[1].removeSuffix("{").trim()
				val struct = parseGLSLStruct(segment)
				data.add(DataType.Struct(structName, struct, manual))
			}
		}
	}
}