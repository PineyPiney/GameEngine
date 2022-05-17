package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.util.input.KeyBind
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

interface Interactable: Storable, Updateable {

    var forceUpdate: Boolean
    var hover: Boolean
    var pressed: Boolean

    val children: MutableList<Interactable>

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

    fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean

    fun onCursorMove(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2){
        if(pressed){
            onDrag(game, cursorPos, cursorDelta)
        }
        for (child in children) {
            if(child.shouldUpdate()) child.onCursorMove(game, cursorPos, cursorDelta)
        }
    }

    fun onDrag(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2) {}

    fun onScroll(game: IGameLogic, scrollDelta: Vec2): Int{
        for (child in children) {
            if(child.shouldUpdate()) child.onScroll(game, scrollDelta)
        }

        return 0
    }

    fun onInput(game: IGameLogic, input: KeyBind, action: Int, cursorPos: Vec2): Int {
        return when {
            input.matches(game.input.primary) -> onPrimary(game, action, input.mods, cursorPos)
            input.matches(game.input.secondary) -> onSecondary(game, action, input.mods, cursorPos)
            else -> 0
        }
    }

    // This is the default action when an item is clicked
    fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Continue pressing if already pressed, or start pressing on first click
        pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
        return action
    }

    fun onSecondary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int = 0

    fun onType(game: IGameLogic, char: Char): Int = 0

    override fun shouldUpdate(): Boolean {
        return this.hover || this.pressed || this.forceUpdate || this.children.any { child -> child.shouldUpdate() }
    }

    companion object{
        const val INTERRUPT = -1
    }
}