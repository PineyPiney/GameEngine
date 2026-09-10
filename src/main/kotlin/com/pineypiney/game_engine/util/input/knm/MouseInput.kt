package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.GLFWWindow
import com.pineypiney.game_engine.window.WindowI
import glm_.b
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.*

open class MouseInput(val input: Inputs, val window: WindowI) : Input {

	// Cursor
	var lastPos = CursorPosition(Vec2(0f), Vec2(0f), window.size * .5f); private set
	private var cursorOffset = CursorPosition(Vec2(0f), Vec2(0f), Vec2i(0))

	private var firstMouse = true

	// Map of when each button was last pressed, or -1 if released
	private val buttonStates = mutableMapOf<Short, Float>()

	var cursorMoveCallback = { pos: CursorPosition, offset: CursorPosition -> }          // gameEngine.activeScreen.onCursorMove(win, screenPos, cursorOffset)
	var mouseScrollCallback = { scroll: Vec2 -> }
	var mouseButtonCallback = { input: InputState, action: Int -> }

	init {
		glfwSetCursorPosCallback(window.windowHandle, ::cursorPosCallback)
		glfwSetScrollCallback(window.windowHandle, ::scrollCallback)
		glfwSetMouseButtonCallback(window.windowHandle, ::mouseButtonCallback)
	}

	fun cursorPosCallback(window: Long, x: Double, y: Double) {
		val size = GLFWWindow.getSize(window)
		val xf = x.toFloat()
		val yf = y.toFloat()

		val screenSpace = Vec2(xf * 2f / size.x - 1f, 1f - yf * 2f / size.y)
		val newPos = CursorPosition(Vec2(screenSpace.x * size.x / size.y, screenSpace.y), screenSpace, Vec2i(x, size.y - y))

		processCursorPos(newPos)
		cursorMoveCallback(newPos, cursorOffset)
	}

	fun scrollCallback(window: Long, xOffset: Double, yOffset: Double) {
		mouseScrollCallback(Vec2(xOffset, yOffset))
	}

	fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
		processMouseButtons(button.s, action, mods.b)
	}

	private fun processCursorPos(pos: CursorPosition) {
		if (firstMouse) {
			lastPos = pos
			firstMouse = false
			return
		}
		cursorOffset = pos - lastPos
		lastPos = pos
	}

	private fun processMouseButtons(button: Short, action: Int, mods: Byte) {
		buttonStates[button] = if (action == 1) Timer.frameTime.f else -1f
		val input = InputState(button, ControlType.MOUSE, mods)
		mouseButtonCallback(input, action)
	}

	override fun update() {
		for ((button, buttonTime) in buttonStates) {
			if (buttonTime != -1f && Timer.frameTime > buttonTime + 1) {
				val input = InputState(button, ControlType.KEYBOARD, 0)
				mouseButtonCallback(input, GLFW_REPEAT)
			}
		}
	}

	fun getButton(button: Short) = buttonStates[button].let { it != -1f }

	fun setCursorAt(pos: CursorPosition, drag: Boolean = false) {
		window.cursorPos = Vec2d(pos.pixels.x, window.height - pos.pixels.y)
		if (!drag) lastPos = pos
	}

	fun setCursorAt(position: Vec2, drag: Boolean = false){
		setCursorAt(CursorPosition(position, window), drag)
	}
}