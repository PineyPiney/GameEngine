package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.DataType

class VertexVulkanShaderData(
	name: String,
	val inputs: MutableMap<Int, Variable>,
	uniforms: List<VulkanBufferObject>,
	storages: List<VulkanBufferObject>,
	pushConstants: Pair<String, DataType.PushConstants>?
) : VulkanShaderData(name, uniforms, storages, pushConstants)