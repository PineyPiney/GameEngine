package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.cameras.ICamera
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import glm_.vec2.Vec2

interface IGameLogic {

    val gameEngine: GameEngine
    val camera: ICamera
    val renderer: GameRenderer<*>
    val gameObjects: ObjectCollection

    val input: Inputs

    val window get() = gameEngine.window

    @Throws(Exception::class)
    fun init()

    fun open()

    fun onCursorMove(window: Window, cursorPos: Vec2, cursorDelta: Vec2)

    fun onScroll(window: Window, scrollDelta: Vec2): Int

    fun onInput(state: InputState, action: Int): Int

    fun onType(char: Char): Int

    fun update(interval: Float, input: Inputs)

    fun updateAspectRatio(window: Window)

    fun add(o: Storable?)

    fun remove(o: Storable?)

    fun render(window: Window, tickDelta: Double)

    fun cleanUp()
}