package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import glm_.b
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.*

class GLFWMouseInput(inputs: Inputs, window: Window) : MouseInput(inputs, window) {

	var firstMouse: Boolean = true

	init {
		glfwSetCursorPosCallback(window.windowHandle, ::cursorPosCallback)
		glfwSetScrollCallback(window.windowHandle, ::scrollCallback)
		glfwSetMouseButtonCallback(window.windowHandle, ::mouseButtonCallback)
	}

	fun cursorPosCallback(window: Long, x: Double, y: Double) {
		val size = this.window.size
		val xf = x.toFloat()
		val yf = y.toFloat()
		val inv = Vec2(2f / (size.x - 1), -2f / (size.y - 1))

		val screenSpace = Vec2(xf * inv.x - 1f, 1f + yf * inv.y)
		val newPos = CursorPosition(Vec2(screenSpace.x * this.window.aspectRatio, screenSpace.y), screenSpace, Vec2i(x, size.y - 1 - y))

		processCursorPos(newPos)
		cursorMoveCallback(newPos, cursorOffset)
	}

	fun scrollCallback(window: Long, xOffset: Double, yOffset: Double) {
		mouseScrollCallback(Vec2(xOffset, yOffset))
	}

	fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
		processMouseButtons(button.s, action, mods.b)
	}

	protected fun processCursorPos(pos: CursorPosition) {
		if (firstMouse) {
			lastPos = pos
			firstMouse = false
			return
		}
		cursorOffset = pos - lastPos
		lastPos = pos
	}
}