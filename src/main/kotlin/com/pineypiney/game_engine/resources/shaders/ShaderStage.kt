package com.pineypiney.game_engine.resources.shaders

import org.lwjgl.opengl.GL43C
import org.lwjgl.util.shaderc.Shaderc
import org.lwjgl.vulkan.NVRayTracing
import org.lwjgl.vulkan.VK10

enum class ShaderStage(val isGraphics: Boolean, val isCompute: Boolean, val opengl: Int, val vulkan: Int, val shaderc: Int) {
	VERTEX(true, false, GL43C.GL_VERTEX_SHADER, VK10.VK_SHADER_STAGE_VERTEX_BIT, Shaderc.shaderc_vertex_shader),
	FRAGMENT(true, false, GL43C.GL_FRAGMENT_SHADER, VK10.VK_SHADER_STAGE_FRAGMENT_BIT, Shaderc.shaderc_fragment_shader),
	TESS_CTRL(true, false, GL43C.GL_TESS_CONTROL_SHADER, VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT, Shaderc.shaderc_tess_control_shader),
	TESS_EVAL(true, false, GL43C.GL_TESS_EVALUATION_SHADER, VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT, Shaderc.shaderc_tess_evaluation_shader),
	GEOMETRY(true, false, GL43C.GL_GEOMETRY_SHADER, VK10.VK_SHADER_STAGE_GEOMETRY_BIT, Shaderc.shaderc_geometry_shader),
	COMPUTE(false, true, GL43C.GL_COMPUTE_SHADER, VK10.VK_SHADER_STAGE_COMPUTE_BIT, Shaderc.shaderc_compute_shader),

	RAY_GEN(false, false, 0, NVRayTracing.VK_SHADER_STAGE_RAYGEN_BIT_NV, Shaderc.shaderc_raygen_shader),
	ANY_HIT(false, false, 0, NVRayTracing.VK_SHADER_STAGE_ANY_HIT_BIT_NV, Shaderc.shaderc_anyhit_shader),
	CLOSEST_HIT(false, false, 0, NVRayTracing.VK_SHADER_STAGE_CLOSEST_HIT_BIT_NV, Shaderc.shaderc_closesthit_shader),
	MISS(false, false, 0, NVRayTracing.VK_SHADER_STAGE_MISS_BIT_NV, Shaderc.shaderc_miss_shader),
	INTERSECTION(false, false, 0, NVRayTracing.VK_SHADER_STAGE_INTERSECTION_BIT_NV, Shaderc.shaderc_intersection_shader),
	CALLABLE(false, false, 0, NVRayTracing.VK_SHADER_STAGE_CALLABLE_BIT_NV, Shaderc.shaderc_callable_shader),
}