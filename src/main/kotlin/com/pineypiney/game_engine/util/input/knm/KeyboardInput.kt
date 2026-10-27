package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.util.input.Input
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window

abstract class KeyboardInput(override val inputs: Inputs, val window: Window) : Input {

	var keyCallback: (state: InputState, action: Int) -> Unit = {_, _ -> }
	var charCallback: (codepoint: Int) -> Unit = {}
}