package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.input.GLFWInputs
import com.pineypiney.game_engine.util.input.Inputs
import org.lwjgl.glfw.GLFW

open class VulkanWindow(title: String, width: Int = 960, height: Int = 540, hints: Map<Int, Int> = defaultVulkanHints) : GLFWWindow(title, width, height, false, false, hints) {
	override val input: Inputs = GLFWInputs(this)

	override fun init() {
		// Make the window visible
		GLFW.glfwShowWindow(windowHandle)
		super.init()
	}

	companion object {
		val defaultVulkanHints: Map<Int, Int> = defaultHints + (GLFW.GLFW_CLIENT_API to GLFW.GLFW_NO_API)
	}
}