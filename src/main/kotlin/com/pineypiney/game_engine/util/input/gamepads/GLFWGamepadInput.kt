package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.input.Inputs
import org.lwjgl.glfw.GLFW.*

open class GLFWGamepadInput(inputs: Inputs, key: ((Int, Int) -> Unit)? = null) : GamepadInput(inputs) {

	override val connectedGamepads: MutableSet<GLFWGamePad> = mutableSetOf()

	// This callback is called whenever a gamepad is connected
	private val joystickCallback: (Int, Int) -> Unit = key ?: { jid: Int, event: Int ->
		when (event) {
			GLFW_CONNECTED -> onControllerConnect(jid)
			GLFW_DISCONNECTED -> onControllerDisconnect(jid)
		}
	}

	init {
		glfwSetJoystickCallback(joystickCallback)

		// GLFW supports up to 16 connected gamepads, at the beginning iterate through each and check if it is connected
		for (i in 0..15) {
			if (glfwJoystickPresent(i)) connectedGamepads.add(createController(i))
		}
	}

	fun onControllerConnect(jid: Int) {
		val gamepad = createController(jid)
		connectedGamepads.add(gamepad)
		gamepadConnectCallback(gamepad)
	}

	fun onControllerDisconnect(jid: Int) {
		val gamepad = connectedGamepads.firstOrNull { it.jid == jid } ?: return
		connectedGamepads.remove(gamepad)
		gamepadDisconnectCallback(gamepad)
	}

	fun createController(id: Int): GLFWGamePad {
		return GLFWGamePad(id, this)
	}

	override fun delete() {

	}
}