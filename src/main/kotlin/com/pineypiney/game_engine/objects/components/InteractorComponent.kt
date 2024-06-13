package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

interface InteractorComponent: ComponentI {

    var hover: Boolean
    var pressed: Boolean
    var forceUpdate: Boolean
    var importance: Int

    val interactableChildren: Collection<InteractorComponent> get() = parent.children.flatMap { it.children.filterIsInstance<InteractorComponent>() }

    val renderSize get() = parent.getComponent<RenderedComponent>()?.renderSize ?: Vec2(1f, 1f)


    fun checkHover(ray: Ray, screenPos: Vec2): Boolean{
        val renderer = parent.getComponent<RenderedComponent>()
        if(renderer != null){
            val shape = renderer.shape as? Rect2D
            if(shape != null){
                val unitSize = Vec2(parent.transformComponent.worldScale) * renderer.renderSize
                return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition) + (shape.origin * unitSize), unitSize * shape.size)
            }
        }
        return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition), Vec2(parent.transformComponent.worldScale) * renderSize)
    }

    fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray){
        if(pressed) onDrag(window, cursorPos, cursorDelta, ray)
    }
    fun onCursorEnter(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray){}
    fun onCursorExit(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray){}
    fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {}
    fun onScroll(window: WindowI, scrollDelta: Vec2): Int = 0

    fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
        return when {
            input.i == 0 && input.controlType == ControlType.MOUSE -> onPrimary(window, action, input.mods, cursorPos)
            input.i == 1 && input.controlType == ControlType.MOUSE -> onSecondary(window, action, input.mods, cursorPos)
            else -> 0
        }
    }

    fun onType(window: WindowI, char: Char): Int = 0

    // This is the default action when an item is clicked
    fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Continue pressing if already pressed, or start pressing on first click
        pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
        return action
    }

    fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int = action

    fun shouldUpdate(): Boolean {
        return this.hover || this.pressed || this.forceUpdate || this.interactableChildren.any { child -> child.shouldUpdate() }
    }

    companion object{
        const val INTERRUPT = -1
    }
}