package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.window.WindowI
import glm_.and
import glm_.i
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2

abstract class Inputs(val window: WindowI) {

	abstract val keyboard: KeyboardInput
	abstract val mouse: MouseInput
	abstract val gamepad: GamepadInput

	var cursorMoveCallback =
		{ _: CursorPosition, _: CursorPosition -> }          // gameEngine.activeScreen.onCursorMove(win, screenPos, cursorOffset)
	var mouseScrollCallback =
		{ _: Vec2 -> }                 // gameEngine.activeScreen.onScroll(Window.getWindow(handle), scrollOffset)
	var keyPressCallback =
		{ _: InputState, _: Int -> }                 // gameEngine.activeScreen.onInput(input, action)
	var keyboardCharCallback = { _: Int -> }                            // gameEngine.activeScreen.onType(input)

	open val primary = InputState(GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)
	open val secondary = InputState(GLFW_MOUSE_BUTTON_2, ControlType.MOUSE)

	var modStates: Byte = 0

	open fun input() {
		gamepad.input()
		mouse.update(Timer.frameTime)
	}

	open fun onInput(key: Short, action: Int, type: ControlType, mods: Byte? = null) {
		// First check if the key is a mod key and if so update the mods map
		mods?.let { modStates = it }

		val input = InputState(key, type, mods ?: modStates)

		keyPressCallback(input, action)
	}

	fun getMod(mod: Number) = (modStates and mod.i) > 1
}