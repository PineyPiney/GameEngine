package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

interface Interactable: Updateable {

    var forceUpdate: Boolean
    var hover: Boolean
    var pressed: Boolean

    val children: MutableSet<Interactable>

    // Importance is used to set the order in which items are updated and interacted with
    // For example, in the level selection screen the buttons are clicked before the entries are
    val importance: Int

    fun addChild(vararg child: Interactable){
        this.children.addAll(child)
    }
    fun addChildren(children: Iterable<Interactable>){
        this.children.addAll(children)
    }
    fun removeChild(vararg child: Interactable){
        this.children.removeAll(child.toSet())
    }
    fun removeChildren(children: Iterable<Interactable>){
        this.children.removeAll(children.toSet())
    }

    fun checkHover(ray: Ray, screenPos: Vec2): Boolean

    fun onCursorMove(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2){
        if(pressed) onDrag(game, cursorPos, cursorDelta)

        val ray = game.camera.getRay()
        for (child in children) {
            child.hover = child.checkHover(ray, cursorPos)
            if(child.shouldUpdate()) child.onCursorMove(game, cursorPos, cursorDelta)
        }
    }

    fun onDrag(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2) {}

    fun onScroll(game: IGameLogic, scrollDelta: Vec2): Int{
        for (child in children.sortedByDescending { it.importance }) {
            if(child.shouldUpdate()) {
                if(child.onScroll(game, scrollDelta) == INTERRUPT) return INTERRUPT
            }
        }
        return 0
    }

    fun onInput(game: IGameLogic, input: InputState, action: Int, cursorPos: Vec2): Int {
        for(child in children.sortedByDescending { it.importance }){
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

    fun onType(game: IGameLogic, char: Char): Int{
        for (child in children.sortedByDescending { it.importance }) {
            if(child.shouldUpdate()) {
                if(child.onType(game, char) == INTERRUPT) return INTERRUPT
            }
        }
        return 0
    }

    // This is the default action when an item is clicked
    fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Continue pressing if already pressed, or start pressing on first click
        pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
        return action
    }

    fun onSecondary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int = 0

    override fun shouldUpdate(): Boolean {
        return this.hover || this.pressed || this.forceUpdate || this.children.any { child -> child.shouldUpdate() }
    }

    companion object{
        const val INTERRUPT = -1
    }
}