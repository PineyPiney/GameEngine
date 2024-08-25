package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs

class DefaultWindow(title: String) : Window(title, 960, 540, false, false) {
	override val input: Inputs = DefaultInput(this)
}