package com.pineypiney.game_engine

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback

class LibrarySetUp {

	companion object {

		fun initLibraries() {
			initOpenGL()
			initOpenAL()
		}

		fun initOpenGL() {
			GLFWErrorCallback.createPrint(System.err).set()

			// Initialize GLFW. Most GLFW functions will not work before doing this.
			check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
		}

		fun initOpenAL() {

		}
	}
}