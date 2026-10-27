package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.input.GLFWInputs
import glm_.i
import kool.map
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil

object GLFWManager : WindowApi {

	override fun init() {
		GLFWErrorCallback.createPrint(System.err).set()

		// Initialise GLFW. Most GLFW functions will not work before doing this.
		check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
	}

	override fun createWindow(title: String, width: Int, height: Int, properties: WindowProperties): Window {
		val hints = mutableMapOf(
			GLFW.GLFW_VISIBLE to properties.visible.i, // the window will stay hidden after creation
			GLFW.GLFW_DECORATED to properties.decorated.i, // the window will be resizable
		)

		for(flag in properties.flags) hints[flag.glfw] = 1

		when(val graphics = properties.graphicsApi){
			is WindowProperties.Graphics.OPENGL -> hints.putAll(listOf(
				GLFW.GLFW_CONTEXT_VERSION_MAJOR to graphics.major,
				GLFW.GLFW_CONTEXT_VERSION_MINOR to graphics.minor,
				GLFW.GLFW_OPENGL_PROFILE to GLFW.GLFW_OPENGL_CORE_PROFILE,
				GLFW.GLFW_OPENGL_FORWARD_COMPAT to 1,
				GLFW.GLFW_SAMPLES to graphics.multisamples
			))
			is WindowProperties.Graphics.VULKAN -> hints[GLFW.GLFW_CLIENT_API] = GLFW.GLFW_NO_API
		}

		val window = GLFWWindow(title, width, height, properties.fullscreen, properties.vSync, ::GLFWInputs, properties.graphicsApi is WindowProperties.Graphics.OPENGL, hints)

		if(properties.graphicsApi is WindowProperties.Graphics.OPENGL) {
			// Make the OpenGL context current
			GLFW.glfwMakeContextCurrent(window.windowHandle)

			GL.createCapabilities()

			if (properties.vSync) {
				// Enable v-sync
				GLFW.glfwSwapInterval(1)
			}
		}

		// Make the window visible
		GLFW.glfwShowWindow(window.windowHandle)

		return window
	}

	override fun getVulkanExtensions(): List<String> {
		val glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions() ?: throw RuntimeException("Missing required GLFW extensions")
		return glfwExtensions.map(MemoryUtil::memUTF8)
	}

	override fun delete() { GLFW.glfwTerminate() }
}