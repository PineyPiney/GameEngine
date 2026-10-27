package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.util.input.gamepads.SDLGamepadInput
import com.pineypiney.game_engine.util.input.knm.SDLKeyboardInput
import com.pineypiney.game_engine.util.input.knm.SDLMouseInput
import com.pineypiney.game_engine.window.Window

class SDLInputs(window: Window): Inputs() {

	val keyboard = SDLKeyboardInput(this, window)
	val mouse = SDLMouseInput(this, window)
	val gamepad = SDLGamepadInput(this)

	override val inputs: Collection<Input> = listOf(keyboard, mouse, gamepad)
}