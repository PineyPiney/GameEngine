package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

interface Interactable: Updateable {

    var forceUpdate: Boolean
    var hover: Boolean
    var pressed: Boolean

    val interactableChildren: Set<Interactable> get() = (this as GameObject).children.filterIsInstance<Interactable>().toSet()

    // Importance is used to set the order in which items are updated and interacted with
    // For example, in the level selection screen the buttons are clicked before the entries are
    val importance: Int

    fun checkHover(ray: Ray, screenPos: Vec2): Boolean

    fun onCursorMove(game: GameLogicI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray){
        if(pressed) onDrag(game, cursorPos, cursorDelta)

        for (child in interactableChildren) {
            child.hover = child.checkHover(ray, cursorPos)
            if(child.shouldUpdate()) child.onCursorMove(game, cursorPos, cursorDelta, ray)
        }
    }

    fun onDrag(game: GameLogicI, cursorPos: Vec2, cursorDelta: Vec2) {}

    fun onScroll(game: GameLogicI, scrollDelta: Vec2): Int{
        for (child in interactableChildren.sortedByDescending { it.importance }) {
            if(child.shouldUpdate()) {
                if(child.onScroll(game, scrollDelta) == INTERRUPT) return INTERRUPT
            }
        }
        return 0
    }

    fun onInput(game: GameLogicI, input: InputState, action: Int, cursorPos: Vec2): Int {
        for(child in interactableChildren.sortedByDescending { it.importance }){
            if(child.shouldUpdate()){
                if(child.onInput(game, input, action, cursorPos) == INTERRUPT) return INTERRUPT
            }
        }
        return when {
            input.i == 0 && input.controlType == ControlType.MOUSE -> onPrimary(game, action, input.mods, cursorPos)
            input.i == 1 && input.controlType == ControlType.MOUSE -> onSecondary(game, action, input.mods, cursorPos)
            else -> 0
        }
    }

    fun onType(game: GameLogicI, char: Char): Int{
        for (child in interactableChildren.sortedByDescending { it.importance }) {
            if(child.shouldUpdate()) {
                if(child.onType(game, char) == INTERRUPT) return INTERRUPT
            }
        }
        return 0
    }

    // This is the default action when an item is clicked
    fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Continue pressing if already pressed, or start pressing on first click
        pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
        return action
    }

    fun onSecondary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int = 0

    override fun shouldUpdate(): Boolean {
        return this.hover || this.pressed || this.forceUpdate || this.interactableChildren.any { child -> child.shouldUpdate() }
    }

    companion object{
        const val INTERRUPT = -1
    }
}