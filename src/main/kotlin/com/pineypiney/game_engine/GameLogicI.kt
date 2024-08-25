package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.GameRendererI
import com.pineypiney.game_engine.util.input.Inputs

interface GameLogicI {

	val gameEngine: GameEngineI<*>
	val renderer: GameRendererI<*>
	val gameObjects: ObjectCollection

	@Throws(Exception::class)
	fun init()

	fun open()

	fun update(interval: Float, input: Inputs)

	fun render(tickDelta: Double)

	fun add(o: GameObject?)

	fun remove(o: GameObject?)

	fun cleanUp()
}