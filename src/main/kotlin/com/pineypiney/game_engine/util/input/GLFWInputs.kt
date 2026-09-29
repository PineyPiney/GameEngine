package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.util.input.gamepads.GamepadInput
import com.pineypiney.game_engine.util.input.gamepads.NativeGamepadInput
import com.pineypiney.game_engine.util.input.knm.KeyboardInput
import com.pineypiney.game_engine.util.input.knm.MouseInput
import com.pineypiney.game_engine.window.WindowI

class GLFWInputs(window: WindowI) : Inputs() {

	val keyboard: KeyboardInput = KeyboardInput(this, window)
	val mouse: MouseInput = MouseInput(this, window)
	val gamepad: GamepadInput = NativeGamepadInput(this)

	override val inputs: Collection<Input> = setOf(
		keyboard, mouse, gamepad
	)
}