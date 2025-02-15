package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.window.WindowI

class DefaultInput(window: WindowI) : Inputs(window) {

	override val keyboard: KeyboardInput = KeyboardInput(this)
	override val mouse: MouseInput = MouseInput(this)
	override val gamepad: GamepadInput = GamepadInput(this)

}