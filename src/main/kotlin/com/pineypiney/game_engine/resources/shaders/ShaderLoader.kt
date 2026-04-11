package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addToMapOr
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.vulkan.pipeline.VulkanComputePipeline
import glm_.bool
import kool.free
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.shaderc.Shaderc
import org.lwjgl.util.shaderc.ShadercIncludeResolve
import org.lwjgl.util.shaderc.ShadercIncludeResult
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer


class ShaderLoader private constructor() : Deletable {

	// This map stores the bytebuffer codes of each shader file
	private val shaderMap: MutableMap<ResourceKey, SubShader> = mutableMapOf()
	val shaderModules: MutableMap<ResourceKey, Long> = mutableMapOf()

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
			streams.engine.resourcesLoader.factory.createSubShader(streams.engine.resourcesLoader, fileName, suf, stage, code)
		}
	}

	fun loadShaderOpenGl(name: String, code: String, type: Int) {
		val shader = generateSubShader(name, code, type)
		shaderMap[ResourceKey(name)] = shader
	}

	fun loadShaderVulkan(vulkan: VulkanManager, loader: ResourcesLoader, key: ResourceKey, fileName: String, code: String, stage: Int) {

		val buffer = compileGlslAsSpirv(loader, fileName, code, stage) ?: return

		val shaderCreateInfo = VkShaderModuleCreateInfo.calloc()
			.`sType$Default`()
			.pCode(buffer)

		val pointer = MemoryUtil.memAllocLong(1)
		VK10.vkCreateShaderModule(vulkan.device.device, shaderCreateInfo, null, pointer)
		val shaderModule = pointer[0]
		pointer.free()
		vulkan.deletionQueue.push(shaderModule, VK10::vkDestroyShaderModule)
		shaderModules[key] = shaderModule
	}

	fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, tessCtrlKey: ResourceKey? = null, tessEvalKey: ResourceKey? = null, geometryKey: ResourceKey? = null): RenderShader {
		val vertex: SubShader = shaderMap.getOrElse(vertexKey) {
			GameEngineI.warn("Could not find vertex shader ${vertexKey.key}")
			return RenderShader.brokeShader
		}
		val fragment: SubShader = shaderMap.getOrElse(fragmentKey) {
			GameEngineI.warn("Could not find fragment shader ${fragmentKey.key}")
			return RenderShader.brokeShader
		}
		val tessCtrl: SubShader? = tessCtrlKey?.let {
			shaderMap.getOrElse(it) {
				GameEngineI.warn("Could not find tessellation control shader ${it.key}")
				return RenderShader.brokeShader
			}
		}
		val tessEval: SubShader? = tessEvalKey?.let {
			shaderMap.getOrElse(it) {
				GameEngineI.warn("Could not find tessellation evaluation shader ${it.key}")
				return RenderShader.brokeShader
			}
		}
		val geometry: SubShader? = geometryKey?.let {
			shaderMap.getOrElse(it) {
				GameEngineI.warn("Could not find geometry shader ${it.key}")
				return RenderShader.brokeShader
			}
		}

		return generateShader(vertexKey.key, vertex, fragmentKey.key, fragment, tessCtrlKey?.key, tessCtrl, tessEvalKey?.key, tessEval, geometryKey?.key, geometry)
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

		var warnedTess = false
		var warnedCompute = false

		val versionRegex = Regex("#version\\s+\\d{3}(\\s+core)?[^\\S\\n]*\\n")

		fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): RenderShader {
			return INSTANCE.getShader(vertexKey, fragmentKey, geometryKey)
		}

		operator fun get(vertexKey: ResourceKey, fragmentKey: ResourceKey, tessCtrlKey: ResourceKey? = null, tessEvalKey: ResourceKey? = null, geometryKey: ResourceKey? = null): RenderShader {
			return INSTANCE.getShader(vertexKey, fragmentKey, tessCtrlKey, tessEvalKey, geometryKey)
		}

		operator fun get(computeKey: ResourceKey): ComputeShader {
			return INSTANCE.getComputeShader(computeKey)
		}

		fun addMacro(code: String, name: String): String {
			val versionLocation = versionRegex.find(code)
			return if (versionLocation != null) {
				code.substring(0, versionLocation.range.last) + "\n#define $name" + code.substring(versionLocation.range.last)
			} else code
		}

		fun generateSubShader(name: String, code: String, type: Int): SubShader {

			var openglCode = addMacro(code, "OPENGL")
			openglCode = openglCode.replace("gl_VertexIndex", "gl_VertexID")
			openglCode = openglCode.replace("gl_InstanceIndex", "gl_InstanceID")


			val id = createShaderFromString(openglCode, type, name)

			val uniforms = compileUniforms(code)

			return SubShader(id, uniforms.toMap())

		}

		fun generateShader(vName: String, vertexShader: SubShader, fName: String, fragmentShader: SubShader, tcName: String? = null, tessCtrlShader: SubShader? = null, teName: String? = null, tessEvalShader: SubShader? = null, gName: String? = null, geometryShader: SubShader? = null): RenderShader {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not generate shader because OpenGL has not been loaded")
				return RenderShader(0, "v", "f", uniforms = emptyMap())
			}
			val ID = glCreateProgram()

			// Shader Program
			glAttachShader(ID, vertexShader.id)
			glAttachShader(ID, fragmentShader.id)
			if (tessCtrlShader != null) glAttachShader(ID, tessCtrlShader.id)
			if (tessEvalShader != null) glAttachShader(ID, tessEvalShader.id)
			if (geometryShader != null) glAttachShader(ID, geometryShader.id)
			glLinkProgram(ID)

			// print linking errors if any
			val name = StringBuilder("$vName x $fName")
			if(tcName != null) name.append(" x $tcName")
			if(teName != null) name.append(" x $teName")
			if(gName != null) name.append(" x $gName")
			checkCompileErrors(ID, 0, name.toString())

			// delete the shaders as they're linked into our program now and no longer necessary
//			glDeleteShader(vertexShader.id)
//			glDeleteShader(fragmentShader.id)
//			if (geometryShader != null) glDeleteShader(geometryShader.id)

			val uniforms = vertexShader.uniforms + fragmentShader.uniforms + (tessCtrlShader?.uniforms ?: emptyMap()) + (tessEvalShader?.uniforms ?: emptyMap()) + (geometryShader?.uniforms ?: emptyMap())

			return RenderShader(ID, vName, fName, gName, tcName, teName, uniforms)
		}

		fun generateComputeShader(name: String, shader: SubShader): ComputeShader {
			val ID = glCreateProgram()
			glAttachShader(ID, shader.id)
			glLinkProgram(ID)
			checkCompileErrors(ID, 0, name)
			return ComputeShader(ID, name, shader.uniforms)
		}

		fun generateComputePipelineVulkan(vulkan: VulkanManager, module: Long, constantSize: Int): VulkanComputePipeline {

			val pushConstant = if (constantSize <= 0) null else VkPushConstantRange.calloc(1)
				.size(constantSize)
				.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)

			val pipelineBuilder = VulkanComputePipeline.Builder()
			val pipelineLayout = VkUtil.createPipelineLayout(vulkan.device, vulkan.pLayout, pushConstant)
			val pipeline = pipelineBuilder.setModule(module).setLayout(pipelineLayout).build(vulkan.device)

			pipelineBuilder.delete()
			vulkan.deletionQueue.push(pipeline)
			return pipeline
		}

		fun createShaderFromString(code: String, shaderType: Int, shaderName: String): Int {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("OpenGL is not loaded, cannot create shader")
				return -1
			}

			// Create numerical handle for shader
			val shader = glCreateShader(shaderType)

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
					GL_TESS_CONTROL_SHADER -> "tesselation control"
					GL_TESS_EVALUATION_SHADER -> "tesselation evaluation"
					GL_GEOMETRY_SHADER -> "geometry"
					GL_COMPUTE_SHADER -> "compute"
					else -> "unidentified"
				}

				success = glGetShaderi(shader, GL_COMPILE_STATUS).bool
				if (!success) {
					infoLog = glGetShaderInfoLog(shader)
					GameEngineI.warn("Could not compile $type shader $shaderName \n$infoLog")
				}
			}
		}


		private fun vulkanStageToShadercKind(stage: Int): Int {
			when (stage) {
				VK13.VK_SHADER_STAGE_VERTEX_BIT -> return Shaderc.shaderc_vertex_shader
				VK13.VK_SHADER_STAGE_FRAGMENT_BIT -> return Shaderc.shaderc_fragment_shader
				VK13.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT -> return Shaderc.shaderc_tess_control_shader
				VK13.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT -> return Shaderc.shaderc_tess_evaluation_shader
				VK13.VK_SHADER_STAGE_GEOMETRY_BIT -> return Shaderc.shaderc_geometry_shader
				VK13.VK_SHADER_STAGE_COMPUTE_BIT -> return Shaderc.shaderc_compute_shader

				NVRayTracing.VK_SHADER_STAGE_RAYGEN_BIT_NV -> return Shaderc.shaderc_raygen_shader
				NVRayTracing.VK_SHADER_STAGE_ANY_HIT_BIT_NV -> return Shaderc.shaderc_anyhit_shader
				NVRayTracing.VK_SHADER_STAGE_CLOSEST_HIT_BIT_NV -> return Shaderc.shaderc_closesthit_shader
				NVRayTracing.VK_SHADER_STAGE_MISS_BIT_NV -> return Shaderc.shaderc_miss_shader
				NVRayTracing.VK_SHADER_STAGE_INTERSECTION_BIT_NV -> return Shaderc.shaderc_intersection_shader
				else -> throw IllegalArgumentException("Stage: " + stage)
			}
		}

		fun compileGlslAsSpirv(loader: ResourcesLoader, fileName: String, code: String, stage: Int): ByteBuffer? {

			// VULKAN is automatically defined I guess?
//			var vulkanCode = addMacro(code, "VULKAN")
			var vulkanCode = code.replace("gl_VertexID", "gl_VertexIndex")
			vulkanCode = vulkanCode.replace("gl_InstanceID", "gl_InstanceIndex")

			val buffer = MemoryUtil.memUTF8(vulkanCode, false)

			val compiler = Shaderc.shaderc_compiler_initialize()
			val options = Shaderc.shaderc_compile_options_initialize()

			val resolver: ShadercIncludeResolve = Resolver(loader, fileName.substringBeforeLast('/') + '/')
			val releaser: ShadercIncludeResultRelease = Releaser()
			Shaderc.shaderc_compile_options_set_target_env(options, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_2)
			Shaderc.shaderc_compile_options_set_target_spirv(options, Shaderc.shaderc_spirv_version_1_4)
			Shaderc.shaderc_compile_options_set_optimization_level(options, Shaderc.shaderc_optimization_level_performance)
			Shaderc.shaderc_compile_options_set_include_callbacks(options, resolver, releaser, 0L)

			val res = Shaderc.shaderc_compile_into_spv(compiler, buffer, vulkanStageToShadercKind(stage), MemoryUtil.memUTF8(fileName), MemoryUtil.memUTF8("main"), options)
			val returnBytes: ByteBuffer?

			if (Shaderc.shaderc_result_get_compilation_status(res) == Shaderc.shaderc_compilation_status_success) {
				val size = Shaderc.shaderc_result_get_length(res).toInt()
				returnBytes = BufferUtils.createByteBuffer(size)
					.put(Shaderc.shaderc_result_get_bytes(res))
					.flip()
			} else {
				returnBytes = null
				// TODO
//				GameEngineI.logger.warn("Could not compile shader $fileName to spirv: ${Shaderc.shaderc_result_get_error_message(res)}")
			}

			Shaderc.shaderc_result_release(res)
			Shaderc.shaderc_compiler_release(compiler)
			resolver.free()
			releaser.free()

			return returnBytes
		}

	}

	class Resolver(val loader: ResourcesLoader, val fileLocation: String) : ShadercIncludeResolve() {
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