package com.pineypiney.game_engine.window

import org.lwjgl.glfw.GLFWVidMode

class GLFWVideoMode(val glfw: GLFWVidMode) : VideoMode {
	override val width: Int get() = glfw.width()
	override val height: Int get() = glfw.height()
	override val refreshRate: Float get() = glfw.refreshRate().toFloat()
}