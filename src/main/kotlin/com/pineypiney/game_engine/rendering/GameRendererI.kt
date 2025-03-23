package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI

interface GameRendererI<E : GameLogicI> : RendererI {
	fun render(game: E, tickDelta: Double)
}