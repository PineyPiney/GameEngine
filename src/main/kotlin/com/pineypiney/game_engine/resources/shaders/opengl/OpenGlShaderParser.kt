package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.GLSLCodeSegment
import com.pineypiney.game_engine.resources.shaders.vulkan.ShaderParser

class OpenGlShaderParser : ShaderParser() {

	val uniforms = mutableMapOf<String, String>()
	val ssbos = mutableMapOf<String, Int>()

	override fun parseLayout(segment: GLSLCodeSegment, params: Map<String, String>, parts: List<String>, manual: Boolean) {
		if (parts.contains("buffer")) {
			if (params.containsKey("binding")) {
//				val storageBlock = DataType.Struct(parts[1], parseGLSLStruct(segment), manual)
				val nextSegment = segments[i + 1]
				val storageName = if (nextSegment.code.all(::isName)) {
					i++
					nextSegment.code
				} else parts.last()
				ssbos[storageName] = params["binding"]!!.toInt()
			}
		}
	}

	fun addUniform(type: DataType, name: String) {
		if (type is DataType.Struct) {
			for ((n, p) in type.variables) {
				addUniform(p.first, "$name.$n")
			}
		} else uniforms[name] = type.toString()
	}

	override fun platformParse(segment: GLSLCodeSegment, parts: List<String>, manual: Boolean) {
		when (parts[0]) {
			// Parse Struct
			"struct" -> {
				val structName = parts[1].removeSuffix("{").trim()
				val struct = parseGLSLStruct(segment)
				data.add(DataType.Struct(structName, struct, manual))
			}

			"uniform" -> {
				if (!manual) {
					val type = parseDataType(parts[1], false)
					if (type != null) addUniform(type, parts[2])
				}
			}
		}
	}
}