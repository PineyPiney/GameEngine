package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs

open class DefaultWindow(title: String, width: Int = 960, height: Int = 540, hints: Map<Int, Int> = defaultHints) : Window(title, width, height, false, false, hints) {
	override val input: Inputs = DefaultInput(this)
}