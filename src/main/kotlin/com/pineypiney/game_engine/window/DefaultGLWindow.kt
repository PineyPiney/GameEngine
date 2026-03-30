package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL

open class DefaultGLWindow(title: String, width: Int = 960, height: Int = 540, hints: Map<Int, Int> = defaultGLHints) : Window(title, width, height, false, false, hints) {
	override val input: Inputs = DefaultInput(this)

	override fun init() {
		// Make the OpenGL context current
		GLFW.glfwMakeContextCurrent(windowHandle)

		// Make the window visible
		GLFW.glfwShowWindow(windowHandle)

		GL.createCapabilities()

		if (vSync) {
			// Enable v-sync
			GLFW.glfwSwapInterval(1)
		}

		super.init()
	}

	override fun update() {
		GLFW.glfwSwapBuffers(windowHandle)
		super.update()
	}

	companion object {
		val defaultGLHints: Map<Int, Int> = defaultHints + mapOf(
			GLFW.GLFW_CONTEXT_VERSION_MAJOR to 4,
			GLFW.GLFW_CONTEXT_VERSION_MINOR to 1,
			GLFW.GLFW_OPENGL_PROFILE to GLFW.GLFW_OPENGL_CORE_PROFILE,
			GLFW.GLFW_OPENGL_FORWARD_COMPAT to 1,
		)
	}
}