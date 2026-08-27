package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.GLFunc
import glm_.vec3.Vec3i

@Suppress("UNUSED")
interface ComputeShader : Shader {

	val compute: ShaderModule

	fun dispatch(api: RenderingApi, x: Int = 1, y: Int = 1, z: Int = 1)

	fun dispatch(api: RenderingApi, groups: Vec3i) {
		dispatch(api, groups.x, groups.y, groups.z)
	}

	override fun getModule(stage: ShaderStage): ShaderModule? {
		return if (stage == ShaderStage.COMPUTE) compute
		else null
	}

	companion object {

		lateinit var missingShader: ComputeShader

		fun initDefaultShader(loader: ResourcesLoader) {

			// Current instance is not capable of compute shaders
			if (GLFunc.isLoaded && !GLFunc.versionAtLeast(4, 3)) return

			val cS = """
				#version 430 core
				
				layout (local_size_x = 16, local_size_y = 16) in;

				layout(rgba16f, set = 0, binding = 0) uniform image2D imgOutput;

				void main(){
					uvec4 value = uvec4(0, 0, 0, 255);
					ivec2 texelCoord = ivec2(gl_GlobalInvocationID.xy);
				
					// the width of the texture
					float width = 1024;
				
					value.x = abs(int(300.0 * (mod(float(texelCoord.x), width) / (gl_NumWorkGroups.x * gl_WorkGroupSize.x))) - 150) + 50;
					value.y = abs(int(300.0 * (mod(float(texelCoord.y) + .5, width) / (gl_NumWorkGroups.y * gl_WorkGroupSize.y))) - 150) + 50;
					imageStore(imgOutput, texelCoord, value);
				}
			""".trimIndent()

			val compute = loader.factory.createShaderModule(loader, "missing_compute", "", ShaderStage.COMPUTE, cS)
			missingShader = loader.factory.createComputeShader(compute)
		}
	}
}