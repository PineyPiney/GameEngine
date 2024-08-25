package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI

interface GameRendererI<E : GameLogicI> : RendererI {

	val numPointLights: Int

	fun render(game: E, tickDelta: Double)

}