package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.WindowI

interface GameLogicI {

    val gameEngine: GameEngineI<*>
    val renderer: RendererI<*>
    val gameObjects: ObjectCollection

    @Throws(Exception::class)
    fun init()

    fun open()

    fun update(interval: Float, input: Inputs)

    fun updateAspectRatio(window: WindowI)

    fun render(tickDelta: Double)

    fun add(o: Storable?)

    fun remove(o: Storable?)

    fun cleanUp()
}