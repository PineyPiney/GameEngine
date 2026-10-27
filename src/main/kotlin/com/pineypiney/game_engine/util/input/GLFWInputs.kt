package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.util.input.gamepads.GLFWGamepadInput
import com.pineypiney.game_engine.util.input.gamepads.GamepadInput
import com.pineypiney.game_engine.util.input.knm.GLFWKeyboardInput
import com.pineypiney.game_engine.util.input.knm.GLFWMouseInput
import com.pineypiney.game_engine.util.input.knm.KeyboardInput
import com.pineypiney.game_engine.util.input.knm.MouseInput
import com.pineypiney.game_engine.window.Window

class GLFWInputs(window: Window) : Inputs() {

	val keyboard: KeyboardInput = GLFWKeyboardInput(this, window)
	val mouse: MouseInput = GLFWMouseInput(this, window)
	val gamepad: GamepadInput = GLFWGamepadInput(this)

	override val inputs: Collection<Input> = setOf(
		keyboard, mouse, gamepad
	)
}