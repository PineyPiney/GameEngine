package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import glm_.f
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.GLFW_REPEAT

abstract class MouseInput(override val inputs: Inputs, val window: Window) : Input {

	// Cursor
	var lastPos: CursorPosition = CursorPosition(Vec2(0f), Vec2(0f), window.size * .5f); protected set
	var cursorOffset: CursorPosition = CursorPosition(Vec2(0f), Vec2(0f), Vec2i(0)); protected set

	// Map of when each button was last pressed, or -1 if released
	val buttonStates = mutableMapOf<Short, Float>()

	var cursorMoveCallback: (pos: CursorPosition, offset: CursorPosition) -> Unit = {_, _ -> }
	var mouseScrollCallback: ( scroll: Vec2) -> Unit = {}
	var mouseButtonCallback: ( input: InputState, action: Int) -> Unit = {_, _ -> }

	protected fun processMouseButtons(button: Short, action: Int, mods: Byte) {
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

	override fun delete() {

	}
}