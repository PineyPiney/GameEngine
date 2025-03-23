package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.window.WindowI
import glm_.b
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.*

open class MouseInput(val input: Inputs) {

	// Cursor
	var lastPos = CursorPosition(Vec2(0f), Vec2(0f), input.window.size * .5f); private set
	private var cursorOffset = CursorPosition(Vec2(0f), Vec2(0f), Vec2i(0))

	private var firstMouse = true

	var transformCoords: (windowSize: Vec2i, xpos: Double, ypos: Double) -> Vec2 = { windowSize, xpos, ypos ->
		val s = 2f / windowSize.y
		Vec2((xpos.toFloat() * s) - (windowSize.x.toFloat() / windowSize.y), 1f - (ypos.toFloat() * s))
	}

	private val buttonStates = mutableMapOf<Short, Float>()

	private val cursorPosCallback = { handle: Long, xpos: Double, ypos: Double ->
		val size = WindowI.getSize(handle)
		val xf = xpos.toFloat()
		val yf = ypos.toFloat()

		val screenSpace = Vec2(xf * 2f / size.x - 1f, 1f - yf * 2f / size.y)
		val newPos = CursorPosition(Vec2(screenSpace.x * size.x / size.y, screenSpace.y), screenSpace, Vec2i(xpos, size.y - ypos))

		processCursorPos(newPos)
		input.cursorMoveCallback(newPos, cursorOffset)
	}
	private val scrollCallback = { _: Long, xOffset: Double, yOffset: Double ->
		input.mouseScrollCallback(Vec2(xOffset, yOffset))
	}
	private val mouseButtonCallback = { _: Long, button: Int, action: Int, mods: Int ->
		processMouseButtons(button.s, action, mods.b)
	}

	init {
		glfwSetCursorPosCallback(input.window.windowHandle, cursorPosCallback)
		glfwSetScrollCallback(input.window.windowHandle, scrollCallback)
		glfwSetMouseButtonCallback(input.window.windowHandle, mouseButtonCallback)
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
		input.onInput(button, action, ControlType.MOUSE, mods)
	}

	fun update(time: Double) {
		for ((button, buttonTime) in buttonStates) {
			if (buttonTime != -1f && time > buttonTime + 1) {
				input.onInput(button, GLFW_REPEAT, ControlType.MOUSE)
			}
		}
	}

	fun getButton(button: Short) = buttonStates[button].let { it != -1f }

	fun setCursorAt(pos: CursorPosition, drag: Boolean = false) {
		input.window.cursorPos = Vec2d(pos.pixels.x, input.window.height - pos.pixels.y)
		if (!drag) lastPos = pos
	}

	fun setCursorAt(position: Vec2, drag: Boolean = false){
		setCursorAt(CursorPosition(position, input.window), drag)
	}
}