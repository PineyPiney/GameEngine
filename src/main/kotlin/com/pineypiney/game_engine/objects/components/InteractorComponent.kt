package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

interface InteractorComponent: ComponentI {

    var hover: Boolean
    var pressed: Boolean
    var forceUpdate: Boolean
    var importance: Int

    val interactableChildren: Collection<InteractorComponent>

    val renderSize: Vec2

    fun checkHover(ray: Ray, screenPos: Vec2): Boolean
    fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray)
    fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray)
    fun onScroll(window: WindowI, scrollDelta: Vec2): Int
    fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int
    fun onType(window: WindowI, char: Char): Int

    // This is the default action when an item is clicked
    fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int
    fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int
    fun shouldUpdate(): Boolean

    companion object{
        const val INTERRUPT = -1
    }
}