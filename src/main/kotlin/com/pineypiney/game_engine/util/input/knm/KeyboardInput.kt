package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.WindowI
import org.lwjgl.glfw.GLFW.glfwSetCharCallback
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback

open class KeyboardInput(val input: Inputs, val window: WindowI) : Input {

	var keyCallback = { state: InputState, action: Int -> }

	var charCallback = { codepoint: Int -> }

	init {
		glfwSetKeyCallback(window.windowHandle, ::keyCallback)
		glfwSetCharCallback(window.windowHandle, ::charCallback)
	}

	fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, modStates: Int) {
		val input = InputState(key, ControlType.KEYBOARD, modStates)
		keyCallback(input, action)
	}

	fun charCallback(window: Long, char: Int) {
		charCallback(char)
	}

	override fun update() {}
}