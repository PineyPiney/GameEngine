package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.shaders.opengl.OpenGlComputeShader
import com.pineypiney.game_engine.resources.shaders.opengl.OpenGlRenderShader
import com.pineypiney.game_engine.resources.shaders.opengl.SubShader
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderBuilder
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderData
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderModule
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addToMapOr
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.splitAndTrimLineBreak
import com.pineypiney.game_engine.util.extension_functions.splitAndTrimWhitespace
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanManager
import glm_.bool
import kool.free
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.shaderc.Shaderc
import org.lwjgl.util.shaderc.ShadercIncludeResolve
import org.lwjgl.util.shaderc.ShadercIncludeResult
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkShaderModuleCreateInfo
import java.nio.ByteBuffer


class ShaderLoader private constructor() : Deletable {

	// This map stores the bytebuffer codes of each shader file
	private val shaderMap: MutableMap<ResourceKey, ShaderModule> = mutableMapOf()

	fun loadShaders(streams: ResourcesLoader.Streams) {

		streams.useEachStream { fileName, stream ->

			val i = fileName.lastIndexOf(".")
			if (i <= 0) return@useEachStream
			val suf = fileName.substring(i + 1)

			val stage = when (suf) {
				"vs" -> ShaderStage.VERTEX
				"fs" -> ShaderStage.FRAGMENT
				"tcs" -> ShaderStage.TESS_CTRL
				"tes" -> ShaderStage.TESS_EVAL
				"gs" -> ShaderStage.GEOMETRY
				"cs" -> ShaderStage.COMPUTE
				else -> return@useEachStream
			}

			val code = stream.readBytes().toString(Charsets.UTF_8)
			streams.engine.resourcesLoader.factory.createShaderModule(streams.engine.resourcesLoader, fileName, suf, stage, code)
		}
	}

	fun loadShaderModuleOpenGl(name: String, code: String, stage: ShaderStage): SubShader {
		val subshader = generateSubShaderOpenGl(name, code, stage)
		shaderMap[ResourceKey(name)] = subshader
		return subshader
	}

	fun loadShaderModuleVulkan(vulkan: VulkanManager, loader: ResourcesLoader, key: ResourceKey, fileName: String, code: String, stage: ShaderStage): ShaderModule {

		val buffer = compileGlslAsSpirv(loader, fileName, code, stage) ?: return VulkanShaderModule(VulkanShaderData("Error", emptyList(), emptyList(), null), stage, vulkan.device, 0L)

		MemoryStack.stackPush().use { stack ->
			val shaderCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
				.`sType$Default`()
				.pCode(buffer)

			val pointer = stack.mallocLong(1)
			VkUtil.processResult(VK10.vkCreateShaderModule(vulkan.device.device, shaderCreateInfo, null, pointer), "Failed to create Shader Module")
			vulkan.device.nameObject(pointer[0], VK10.VK_OBJECT_TYPE_SHADER_MODULE, key.key)

			val shaderBuilder = VulkanShaderBuilder(stage)
			val shaderData = shaderBuilder.parseUniformsVulkan(key.key, code)

			val shaderModule = VulkanShaderModule(shaderData, stage, vulkan.device, pointer[0])
			shaderMap.put(key, shaderModule)?.delete()
			return shaderModule
		}
	}


	fun getSubShader(id: ResourceKey) = shaderMap[id]

	fun getRenderShader(
		vertexKey: ResourceKey,
		fragmentKey: ResourceKey,
		optional: Iterable<ResourceKey>,
		parameters: RenderShaderParameters,
		deletionQueue: DeletionQueue = DeletionQueue.GLOBAL
	): RenderShader {
		val vertex: ShaderModule = shaderMap.getOrElse(vertexKey) {
			GameEngineI.warn("Could not find vertex shader ${vertexKey.key}")
			return RenderShader.missing
		}
		val fragment: ShaderModule = shaderMap.getOrElse(fragmentKey) {
			GameEngineI.warn("Could not find fragment shader ${fragmentKey.key}")
			return RenderShader.missing
		}
		val optionalModules = optional.map { key ->
			shaderMap.getOrElse(key) {
				GameEngineI.warn("Could not find shader ${key.key}")
				return RenderShader.missing
			}
		}

		return ResourceFactory.INSTANCE.createRenderShader(vertex, fragment, optionalModules, parameters, deletionQueue)
	}

	fun getComputeShader(computeKey: ResourceKey): ComputeShader {
		val compute: ShaderModule = shaderMap.getOrElse(computeKey) {
			GameEngineI.warn("Could not find vertex shader ${computeKey.key}")
			return ComputeShader.missingShader
		}
		return ResourceFactory.INSTANCE.createComputeShader(compute)
	}

	override fun delete() {
		shaderMap.delete()
		shaderMap.clear()
	}

	companion object {
		val INSTANCE: ShaderLoader = ShaderLoader()

		var warnedTess = false
		var warnedCompute = false

		val versionRegex = Regex("#version\\s+\\d{3}(\\s+core)?[^\\S\\n]*\\n")

		operator fun get(
			vertexKey: ResourceKey,
			fragmentKey: ResourceKey,
			parameters: RenderShaderParameters = RenderShaderParameters(),
			deletionQueue: DeletionQueue = DeletionQueue.GLOBAL
		): RenderShader {
			return INSTANCE.getRenderShader(vertexKey, fragmentKey, emptyList(), parameters, deletionQueue)
		}

		operator fun get(vertexKey: ResourceKey, fragmentKey: ResourceKey, vararg optional: ResourceKey): RenderShader {
			return INSTANCE.getRenderShader(vertexKey, fragmentKey, optional.toList(), RenderShaderParameters())
		}

		operator fun get(vertexKey: ResourceKey, fragmentKey: ResourceKey, optional: List<ResourceKey>, parameters: RenderShaderParameters = RenderShaderParameters()): RenderShader {
			return INSTANCE.getRenderShader(vertexKey, fragmentKey, optional, parameters)
		}

		operator fun get(computeKey: ResourceKey): ComputeShader {
			return INSTANCE.getComputeShader(computeKey)
		}

		fun addMacro(code: String, name: String): String {
			val versionLocation = versionRegex.find(code)
			return if (versionLocation != null) {
				val beforeInsert = code.substring(0, versionLocation.range.last)
				val lineNumber = beforeInsert.count { it == '\n' }
				beforeInsert + "\n#ifndef $name\n#define $name\n#endif\n#line ${lineNumber + 2}" + code.substring(versionLocation.range.last)
			} else code
		}

		fun parseGLSLStruct(iterator: ShaderLineIterator): Map<String, String> {
			val struct = mutableMapOf<String, String>()
			for ((line, _) in iterator) {
				val endBracketIndex = line.indexOf('}')
				if (endBracketIndex == 0) return struct
				else {
					val parts = line.splitAndTrimWhitespace()
					val key = if (endBracketIndex != -1) parts[1].substring(0, endBracketIndex) else parts[1].substringBefore(';')
					struct[key] = parts[0]
					if (endBracketIndex != -1) return struct
				}
			}
			return struct
		}

		// OPENGL

		fun generateSubShaderOpenGl(name: String, code: String, stage: ShaderStage): SubShader {

			var openglCode = addMacro(code, "OPENGL")
			openglCode = openglCode.replace("gl_VertexIndex", "gl_VertexID")
			openglCode = openglCode.replace("gl_InstanceIndex", "gl_InstanceID")


			val handle = createShaderFromString(openglCode, stage, name)

			val uniforms = compileUniforms(code)

			return SubShader(name, stage, handle, uniforms.toMap())

		}

		fun createShaderFromString(code: String, stage: ShaderStage, shaderName: String): Int {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("OpenGL is not loaded, cannot create shader")
				return -1
			}

			// Create numerical handle for shader
			val shader = glCreateShader(stage.opengl)

			glShaderSource(shader, code)
			glCompileShader(shader)
			checkCompileErrorsOpenGl(shader, stage, shaderName)

			return shader
		}

		fun generateGraphicsShaderOpenGl(vertexShader: SubShader, fragmentShader: SubShader, optionalShaders: Iterable<SubShader>, parameters: RenderShaderParameters): OpenGlRenderShader {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not generate shader because OpenGL has not been loaded")
				return OpenGlRenderShader(0, vertexShader, fragmentShader, emptyList(), uniforms = emptyMap(), parameters)
			}
			val ID = glCreateProgram()

			// Shader Program
			glAttachShader(ID, vertexShader.handle)
			glAttachShader(ID, fragmentShader.handle)
			val name = StringBuilder("${vertexShader.getName()} x ${fragmentShader.getName()}")

			val uniforms = vertexShader.uniforms.toMutableMap()
			uniforms.putAll(fragmentShader.uniforms)
			for (subshader in optionalShaders) {
				glAttachShader(ID, subshader.handle)
				uniforms.putAll(subshader.uniforms)
				name.append(" x ${subshader.getName()}")
			}
			glLinkProgram(ID)

			// print linking errors if any
			checkCompileErrorsOpenGl(ID, null, name.toString())


			return OpenGlRenderShader(ID, vertexShader, fragmentShader, optionalShaders, uniforms, parameters)
		}

		fun generateComputeShaderOpenGl(shader: SubShader): OpenGlComputeShader {
			val ID = glCreateProgram()
			glAttachShader(ID, shader.handle)
			glLinkProgram(ID)
			checkCompileErrorsOpenGl(ID, null, shader.getName())
			return OpenGlComputeShader(ID, shader, shader.uniforms)
		}

		fun compileUniforms(code: String): Map<String, String> {
			var structName = ""
			val structs = mutableMapOf<String, MutableMap<String, String>>()
			val uniforms = mutableMapOf<String, String>()

			var skipNext = false
			for (fullLine in code.splitAndTrimLineBreak()) {

				val commentIndex = fullLine.indexOf("//")
				val line = if(commentIndex == -1) fullLine else fullLine.substring(0, commentIndex).trim()

				if(line.isNotEmpty()){
					if(skipNext) {
						skipNext = false
						continue
					}

					val parts = line.splitAndTrimWhitespace()
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

		fun checkCompileErrorsOpenGl(shader: Int, stage: ShaderStage?, shaderName: String) {
			val success: Boolean
			val infoLog: String
			if (stage == null) {
				success = glGetProgrami(shader, GL_LINK_STATUS).bool
				if (!success) {
					infoLog = glGetProgramInfoLog(shader)
					GameEngineI.warn("Could not link shaders $shaderName \n$infoLog")
				}
			} else {

				// Type is used later on in error debugging
				val type = stage.name.lowercase()

				success = glGetShaderi(shader, GL_COMPILE_STATUS).bool
				if (!success) {
					infoLog = glGetShaderInfoLog(shader)
					GameEngineI.warn("Could not compile $type shader $shaderName \n$infoLog")
				}
			}
		}


		// VULKAN

		fun compileGlslAsSpirv(loader: ResourcesLoader, fileName: String, code: String, stage: ShaderStage): ByteBuffer? {

			// VULKAN is automatically defined I guess?
			var vulkanCode = addMacro(code, "VULKAN")
			vulkanCode = vulkanCode.replace("gl_VertexID", "gl_VertexIndex")
			vulkanCode = vulkanCode.replace("gl_InstanceID", "gl_InstanceIndex")

			val buffer = MemoryUtil.memUTF8(vulkanCode, false)

			val compiler = Shaderc.shaderc_compiler_initialize()
			val options = Shaderc.shaderc_compile_options_initialize()

			val resolver: ShadercIncludeResolve = IncludeResolver(loader, fileName.substringBeforeLast('/') + '/')
			val releaser: ShadercIncludeResultRelease = Releaser()
			Shaderc.shaderc_compile_options_set_target_env(options, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_2)
			Shaderc.shaderc_compile_options_set_target_spirv(options, Shaderc.shaderc_spirv_version_1_4)
			Shaderc.shaderc_compile_options_set_optimization_level(options, Shaderc.shaderc_optimization_level_performance)
			Shaderc.shaderc_compile_options_set_include_callbacks(options, resolver, releaser, 0L)

			val res = Shaderc.shaderc_compile_into_spv(compiler, buffer, stage.shaderc, MemoryUtil.memUTF8(fileName), MemoryUtil.memUTF8("main"), options)
			val returnBytes: ByteBuffer?

			if (Shaderc.shaderc_result_get_compilation_status(res) == Shaderc.shaderc_compilation_status_success) {
				val size = Shaderc.shaderc_result_get_length(res).toInt()
				returnBytes = BufferUtils.createByteBuffer(size)
					.put(Shaderc.shaderc_result_get_bytes(res))
					.flip()
			} else {
				returnBytes = null
				// TODO
				GameEngineI.logger.warn("Could not compile shader $fileName to spirv: ${Shaderc.shaderc_result_get_error_message(res)}")
			}

			Shaderc.shaderc_result_release(res)
			Shaderc.shaderc_compiler_release(compiler)
			resolver.free()
			releaser.free()

			return returnBytes
		}

	}

	// For resolving #include macros
	class IncludeResolver(val loader: ResourcesLoader, val fileLocation: String) : ShadercIncludeResolve() {
		override fun invoke(user_data: Long, requested_source: Long, type: Int, requesting_source: Long, include_depth: Long): Long {
			val src = fileLocation + MemoryUtil.memUTF8(requested_source)
			val stream = loader.getStream(src) ?: throw AssertionError("Failed to resolve include $src")
			val res = ShadercIncludeResult.calloc()
				.content(ResourcesLoader.ioResourceToByteBuffer(stream))
				.source_name(MemoryUtil.memUTF8(src))
			return res.address()
		}
	}

	class Releaser : ShadercIncludeResultRelease() {
		override fun invoke(user_data: Long, include_result: Long) {
			val result = ShadercIncludeResult.create(include_result)
			result.source_name().free()
			result.free()
		}
	}
}

typealias ShaderLineIterator = Iterator<GLSLCodeSegment>