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
	var lastPos = Vec2(); private set
	private var firstMouse = true

	var transformCoords: (windowSize: Vec2i, xpos: Double, ypos: Double) -> Vec2 = { windowSize, xpos, ypos ->
		val s = 2.0 / windowSize.y
		Vec2((xpos * s) - (windowSize.x.toDouble() / windowSize.y), 1.0 - (ypos * s))
	}

	private var cursorOffset = Vec2(0.0, 0.0)

	private val buttonStates = mutableMapOf<Short, Float>()

	private val cursorPosCallback = { handle: Long, xpos: Double, ypos: Double ->
		val size = WindowI.getSize(handle)
		val screenPos = transformCoords(size, xpos, ypos)
		processCursorPos(screenPos)
		input.cursorMoveCallback(screenPos, cursorOffset)
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

	private fun processCursorPos(pos: Vec2) {
		if (firstMouse) {
			lastPos = pos
			firstMouse = false
			return
		}
		cursorOffset = Vec2(pos.x - lastPos.x, pos.y - lastPos.y)
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

	fun setCursorAt(pos: Vec2, drag: Boolean = false) {
		input.window.cursorPos = Vec2d(Vec2(pos.x + 1, -pos.y + 1) / 2 * input.window.size)

		if (!drag) lastPos = pos
	}

	fun screenSpaceCursor(): Vec2 = Vec2(lastPos.x / input.window.aspectRatio, lastPos.y)
}