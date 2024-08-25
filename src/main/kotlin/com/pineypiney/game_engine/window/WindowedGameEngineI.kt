//do you have a hernia
package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.util.input.Inputs

interface WindowedGameEngineI<E : GameLogicI> : GameEngineI<E> {

	val window: WindowI
	val input: Inputs

	fun setInputCallbacks()
}
