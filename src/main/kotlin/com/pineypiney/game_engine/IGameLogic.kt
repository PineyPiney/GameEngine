package com.pineypiney.game_engine

import com.pineypiney.game_engine.renderers.GameRenderer
import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.KeyBind
import com.pineypiney.game_engine.visual.ScreenObjectCollection
import com.pineypiney.game_engine.visual.Storable
import glm_.vec2.Vec2

interface IGameLogic {

    val gameEngine: GameEngine
    var camera: Camera
    var renderer: GameRenderer
    var gameObjects: ScreenObjectCollection

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

    fun regenerateFrameBuffers()

    fun add(o: Storable?)

    fun remove(o: Storable?)

    fun render(window: Window, tickDelta: Double)

    fun cleanUp()
}