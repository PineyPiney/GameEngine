package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.cameras.ICamera
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import glm_.vec2.Vec2

interface GameLogicI {

    val gameEngine: GameEngineI<*>
    val camera: ICamera
    val renderer: GameRenderer<*>
    val gameObjects: ObjectCollection

    val input: Inputs

    val window get() = gameEngine.window

    @Throws(Exception::class)
    fun init()

    fun open()

    fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2)

    fun onScroll(scrollDelta: Vec2): Int

    fun onInput(state: InputState, action: Int): Int

    fun onType(char: Char): Int

    fun update(interval: Float, input: Inputs)

    fun updateAspectRatio(window: WindowI)

    fun render(window: WindowI, tickDelta: Double)

    fun add(o: Storable?)

    fun remove(o: Storable?)

    fun cleanUp()
}