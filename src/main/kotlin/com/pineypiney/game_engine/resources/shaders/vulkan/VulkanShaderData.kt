package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.DataType

class VulkanShaderData(val name: String, val structs: List<DataType.CustomType>, val uniforms: List<VulkanUBO>, val pushConstants: Pair<String, DataType.PushConstants>?) {

	fun forAllUniforms(action: (String, DataType) -> Unit) {
		for (uniformBuffer in uniforms) action(uniformBuffer.name, uniformBuffer.data)
		if (pushConstants != null) action(pushConstants.first, pushConstants.second)
	}

	fun getUniformMap(): Map<String, String> {
		val map = mutableMapOf<String, String>()
		forAllUniforms { name, type -> if (!type.manual) type.getUniformMap(name, map) }
		return map
	}
}