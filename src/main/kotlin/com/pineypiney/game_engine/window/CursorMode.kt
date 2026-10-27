package com.pineypiney.game_engine.window

import org.lwjgl.glfw.GLFW

enum class CursorMode(val glfw: Int) {
	NORMAL(GLFW.GLFW_CURSOR_NORMAL),
	HIDDEN(GLFW.GLFW_CURSOR_HIDDEN),
	DISABLED(GLFW.GLFW_CURSOR_DISABLED),
	CAPTURED(GLFW.GLFW_CURSOR_CAPTURED),
}