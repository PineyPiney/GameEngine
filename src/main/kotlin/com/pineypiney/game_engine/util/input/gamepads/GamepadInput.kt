package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import org.lwjgl.glfw.GLFW.*

open class GamepadInput(val input: Inputs, key: ((Int, Int) -> Unit)? = null) : Input {

	// This callback is called whenever a gamepad is connected
	private val joystickCallback: (Int, Int) -> Unit = key ?: { jid: Int, event: Int ->
		when (event) {
			GLFW_CONNECTED -> onControllerConnect(jid)
			GLFW_DISCONNECTED -> onControllerDisconnect(jid)
		}
	}

	// A list of all connected gamepads
	val connectedGamepads = mutableSetOf<GamePad>()

	var gamepadButtonCallback = { state: InputState, action: Int -> }
	var gamepadConnectCallback = { gamepad: GamePad -> }
	var gamepadDisconnectCallback = { gamepad: GamePad -> }

	init {
		glfwSetJoystickCallback(joystickCallback)

		// GLFW supports up to 16 connected gamepads, at the beginning iterate through each and check if it is connected
		for (i in 0..15) {
			if (glfwJoystickPresent(i)) connectedGamepads.add(createController(i))
		}
	}

	// Because GLFW doesn't have a callback for gamepad inputs they must be surveyed every render loop
	override fun update() {
		for (gamepad in connectedGamepads) gamepad.input()
	}

	fun onControllerConnect(jid: Int) {
		val gamepad = createController(jid)
		connectedGamepads.add(gamepad)
		gamepadConnectCallback(gamepad)
	}

	fun onControllerDisconnect(jid: Int) {
		val gamepad = connectedGamepads.firstOrNull { it.id == jid } ?: return
		connectedGamepads.remove(gamepad)
		gamepadDisconnectCallback(gamepad)
	}

	fun createController(id: Int): GamePad {
		val name = glfwGetGamepadName(id)
		return when (name) {
			"PS5 Controller" -> PS5Controller(id, this)
			"PS4 Controller" -> PS4Controller(id, this)
			"XInput Gamepad (GLFW)" -> GamePad(id, this)
			else -> GamePad(id, this)
		}
	}

	fun getController(id: Int): GamePad? = connectedGamepads.firstOrNull { it.id == id }

	inline fun setDefaultController(crossinline current: () -> Int, crossinline set: (GamePad?) -> Unit) {

		gamepadConnectCallback = { if (current.invoke() == -1) set(it) }
		gamepadDisconnectCallback = { if (current.invoke() == it.id) set(null) }

		val gamepad = connectedGamepads.firstOrNull()
		if (gamepad != null) set(gamepad)
	}
}