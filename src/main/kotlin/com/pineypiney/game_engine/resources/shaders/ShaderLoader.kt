package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addToMapOr
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.toString
import glm_.bool
import org.lwjgl.opengl.GL46C.*
import java.io.InputStream

class ShaderLoader private constructor() : Deleteable {

	// This map stores the bytebuffer codes of each shader file
	private val shaderMap: MutableMap<ResourceKey, SubShader> = mutableMapOf()

	fun loadShaders(streams: Map<String, InputStream>) {
		for ((fileName, stream) in streams) {

			val i = fileName.lastIndexOf(".")
			if (i <= 0) continue
			val suf = fileName.substring(i + 1)

			val type = when (suf) {
				"vs" -> GL_VERTEX_SHADER
				"fs" -> GL_FRAGMENT_SHADER
				"gs" -> GL_GEOMETRY_SHADER
				"cs" -> GL_COMPUTE_SHADER
				else -> 0
			}

			if(type == GL_COMPUTE_SHADER && !GLFunc.versionAtLeast(4, 3)){
				if(!warnedCompute){
					GameEngineI.logger.warn("Tried to create Compute Shader, which requires OpenGL 4.3 or higher, but created OpenGL Instance is version ${GLFunc.version.toString(".", Int::toString)}")
					warnedCompute = true
				}
				stream.close()
				continue
			}

			loadShader(fileName.removeSuffix(".$suf"), stream.readBytes(), type)

			stream.close()
		}
	}

	private fun loadShader(name: String, bytes: ByteArray, type: Int) {
		val code = bytes.copyOf().toString(Charsets.UTF_8)
		val shader = generateSubShader(name, code, type)
		shaderMap[ResourceKey(name)] = shader
	}

	fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): RenderShader {
		val vertex: SubShader = shaderMap.getOrElse(vertexKey) {
			GameEngineI.warn("Could not find vertex shader ${vertexKey.key}")
			return RenderShader.brokeShader
		}
		val fragment: SubShader = shaderMap.getOrElse(fragmentKey) {
			GameEngineI.warn("Could not find fragment shader ${fragmentKey.key}")
			return RenderShader.brokeShader
		}
		val geometry: SubShader? = geometryKey?.let {
			shaderMap.getOrElse(it) {
				GameEngineI.warn("Could not find geometry shader ${it.key}")
				return RenderShader.brokeShader
			}
		}

		return generateShader(vertexKey.key, vertex, fragmentKey.key, fragment, geometryKey?.key, geometry)
	}

	fun getComputeShader(computeKey: ResourceKey): ComputeShader {
		val compute: SubShader = shaderMap.getOrElse(computeKey) {
			GameEngineI.warn("Could not find vertex shader ${computeKey.key}")
			return ComputeShader.brokeShader
		}

		return generateComputeShader(computeKey.key, compute)
	}

	override fun delete() {
		shaderMap.delete()
		shaderMap.clear()
	}

	companion object {
		val INSTANCE: ShaderLoader = ShaderLoader()

		var warnedCompute = false

		fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): RenderShader {
			return INSTANCE.getShader(vertexKey, fragmentKey, geometryKey)
		}

		operator fun get(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): RenderShader {
			return INSTANCE.getShader(vertexKey, fragmentKey, geometryKey)
		}

		operator fun get(computeKey: ResourceKey): ComputeShader {
			return INSTANCE.getComputeShader(computeKey)
		}

		fun generateSubShader(name: String, code: String, type: Int): SubShader {

			val id = createShaderFromString(code, type, name)

			val uniforms = compileUniforms(code)

			return SubShader(id, uniforms.toMap())

		}

		fun generateShader(vName: String, vertexShader: SubShader, fName: String, fragmentShader: SubShader, gName: String? = null, geometryShader: SubShader? = null): RenderShader {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not generate shader because OpenGL has not been loaded")
				return RenderShader(0, "v", "f", null, mapOf())
			}
			val ID = glCreateProgram()

			// Shader Program
			glAttachShader(ID, vertexShader.id)
			glAttachShader(ID, fragmentShader.id)
			if (geometryShader != null) glAttachShader(ID, geometryShader.id)
			glLinkProgram(ID)

			// print linking errors if any
			checkCompileErrors(ID, 0, "$vName x $fName" + if (gName != null) " x $gName" else "")

			// delete the shaders as they're linked into our program now and no longer necessary
//			glDeleteShader(vertexShader.id)
//			glDeleteShader(fragmentShader.id)
//			if (geometryShader != null) glDeleteShader(geometryShader.id)

			val uniforms = vertexShader.uniforms + fragmentShader.uniforms + (geometryShader?.uniforms ?: mapOf())

			return RenderShader(ID, vName, fName, gName, uniforms)
		}

		fun generateComputeShader(name: String, shader: SubShader): ComputeShader {
			val ID = glCreateProgram()
			glAttachShader(ID, shader.id)
			glLinkProgram(ID)
			checkCompileErrors(ID, 0, name)
			return ComputeShader(ID, name, shader.uniforms)
		}

		fun createShaderFromString(code: String, shaderType: Int, shaderName: String): Int {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("OpenGL is not loaded, cannot create shader")
				return -1
			}
			// Create numerical handle for shader
			val shader = glCreateShader(shaderType)

			// vertex Shader
			glShaderSource(shader, code)
			glCompileShader(shader)
			checkCompileErrors(shader, shaderType, shaderName)

			return shader
		}

		fun compileUniforms(code: String): Map<String, String> {
			var structName = ""
			val structs = mutableMapOf<String, MutableMap<String, String>>()
			val uniforms = mutableMapOf<String, String>()

			var skipNext = false
			for (fullLine in code.split('\n').map { it.trim() }.filter { it.isNotEmpty() }) {

				val commentIndex = fullLine.indexOf("//")
				val line = if(commentIndex == -1) fullLine else fullLine.substring(0, commentIndex).trim()

				if(line.isNotEmpty()){
					if(skipNext) {
						skipNext = false
						continue
					}
					
					val parts = line.split(' ').map { it.trim() }.filter { it.isNotEmpty() }
					if (structName.isNotEmpty()) {
						if (line[0] == '}') structName = ""
						else {
							val bracket = line.contains('}')
							structs.addToMapOr(
								structName,
								if (line.contains('}')) parts[1].substringBefore('}') else parts[1].substringBefore(';'),
								parts[0]
							)
							if (bracket) structName = ""
						}
					}
					if (parts[0] == "struct") {
						structName = parts[1].removeSuffix("{").trim()
						structs[structName] = mutableMapOf()
					}
					if (parts[0] != "uniform") continue
					val name = line.removePrefix("uniform ${parts[1]} ").substringBefore(';')
					if (structs.containsKey(parts[1])) {
						for ((k, v) in structs[parts[1]]!!) {
							uniforms["$name.$k"] = v
						}
					} else uniforms[name] = parts[1]
				}


				if(commentIndex != -1){
					val comment = fullLine.substring(commentIndex + 2)
					if(comment.contains("MANUAL")) skipNext = true
				}
			}

			return uniforms
		}

		fun checkCompileErrors(shader: Int, shaderType: Int, shaderName: String) {
			val success: Boolean
			val infoLog: String
			if (shaderType == 0) {
				success = glGetProgrami(shader, GL_LINK_STATUS).bool
				if (!success) {
					infoLog = glGetProgramInfoLog(shader)
					GameEngineI.warn("Could not link shaders $shaderName \n$infoLog")
				}
			} else {

				// Type is used later on in error debugging
				val type = when (shaderType) {
					GL_VERTEX_SHADER -> "vertex"
					GL_FRAGMENT_SHADER -> "fragment"
					GL_GEOMETRY_SHADER -> "geometry"
					GL_COMPUTE_SHADER -> "compute"
					else -> return
				}

				success = glGetShaderi(shader, GL_COMPILE_STATUS).bool
				if (!success) {
					infoLog = glGetShaderInfoLog(shader)
					GameEngineI.warn("Could not compile $type shader $shaderName \n$infoLog")
				}
			}
		}
	}
}