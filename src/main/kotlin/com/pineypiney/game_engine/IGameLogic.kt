package com.pineypiney.game_engine

import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.renderers.GameRenderer
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.KeyBind
import glm_.vec2.Vec2

interface IGameLogic {

    val gameEngine: GameEngine
    val camera: Camera
    val renderer: GameRenderer
    val gameObjects: ScreenObjectCollection

    val input: Inputs

    val window get() = gameEngine.window

    @Throws(Exception::class)
    fun init()

    fun open()

    fun onCursorMove(window: Window, cursorPos: Vec2, cursorDelta: Vec2)

    fun onScroll(window: Window, scrollDelta: Vec2)

    fun onInput(key: KeyBind, action: Int)

    fun update(interval: Float, input: Inputs)

    fun updateAspectRatio(window: Window)

    fun add(o: Storable?)

    fun remove(o: Storable?)

    fun render(window: Window, tickDelta: Double)

    fun cleanUp()
}