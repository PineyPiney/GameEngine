package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs

abstract class GamepadInput(val inputs: Inputs) : Input {

	// A list of all connected gamepads
	abstract val connectedGamepads: Set<GamePad>

	var gamepadButtonCallback = { state: InputState, action: Int -> }
	var gamepadConnectCallback = { gamepad: GamePad -> }
	var gamepadDisconnectCallback = { gamepad: GamePad -> }

	// Because GLFW doesn't have a callback for gamepad inputs they must be surveyed every render loop
	override fun update() {
		for (gamepad in connectedGamepads) gamepad.input()
	}

	fun getController(id: Int): GamePad? = connectedGamepads.firstOrNull { it.id == id }

	inline fun setDefaultController(crossinline current: () -> Int, crossinline set: (GamePad?) -> Unit) {

		gamepadConnectCallback = { if (current.invoke() == -1) set(it) }
		gamepadDisconnectCallback = { if (current.invoke() == it.id) set(null) }

		val gamepad = connectedGamepads.firstOrNull()
		if (gamepad != null) set(gamepad)
	}
}