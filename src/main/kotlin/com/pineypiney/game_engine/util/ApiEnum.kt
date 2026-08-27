package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.extension_functions.orOfInt

interface ApiEnum {
	val opengl: Int
	val vulkan: Int
}

fun Iterable<ApiEnum>.openglMask() = orOfInt(ApiEnum::opengl)
fun Iterable<ApiEnum>.vulkanMask() = orOfInt(ApiEnum::vulkan)