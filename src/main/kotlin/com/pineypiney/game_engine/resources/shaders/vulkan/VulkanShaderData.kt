package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.DataType

class VulkanShaderData(val name: String, val structs: List<DataType.CustomType>, val uniforms: Map<String, VulkanUBO>, val pushConstants: Pair<String, DataType.PushConstants>?)