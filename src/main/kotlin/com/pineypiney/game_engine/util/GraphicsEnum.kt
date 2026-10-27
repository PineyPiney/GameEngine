package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.extension_functions.orOfInt

interface GraphicsEnum {
	val opengl: Int
	val vulkan: Int
}

fun Iterable<GraphicsEnum>.openglMask() = orOfInt(GraphicsEnum::opengl)
fun Iterable<GraphicsEnum>.vulkanMask() = orOfInt(GraphicsEnum::vulkan)