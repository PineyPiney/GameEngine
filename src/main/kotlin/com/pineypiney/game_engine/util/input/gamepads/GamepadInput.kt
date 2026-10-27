package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs

abstract class GamepadInput(override val inputs: Inputs) : Input {

	// A list of all connected gamepads
	abstract val connectedGamepads: Set<Gamepad>

	var gamepadButtonCallback = { state: InputState, action: Int -> }
	var gamepadConnectCallback = { gamepad: Gamepad -> }
	var gamepadDisconnectCallback = { gamepad: Gamepad -> }

	// Because GLFW doesn't have a callback for gamepad inputs they must be surveyed every render loop
	override fun update() {
		for (gamepad in connectedGamepads) gamepad.input()
	}

	open fun getController(id: Int): Gamepad? = connectedGamepads.firstOrNull { it.id == id }

	inline fun setDefaultController(crossinline current: () -> Int, crossinline set: (Gamepad?) -> Unit) {

		gamepadConnectCallback = { if (current.invoke() == -1) set(it) }
		gamepadDisconnectCallback = { if (current.invoke() == it.id) set(null) }

		val gamepad = connectedGamepads.firstOrNull()
		if (gamepad != null) set(gamepad)
	}

	override fun delete() {
		connectedGamepads.delete()
	}
}