package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.GameLogicI
import glm_.vec2.Vec2

abstract class CheckBox: InteractableMenuItem() {

    var ticked = false
    abstract val action: (Boolean) -> Unit

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        if(action == 1) toggle()
        return super.onPrimary(game, action, mods, cursorPos)
    }

    fun toggle() {
        ticked = !ticked
        action(ticked)
    }
}