package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.GameLogicI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

abstract class AbstractButton : StaticInteractableMenuItem() {

    var active: Boolean = true
    abstract val action: (button: AbstractButton) -> Unit

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val ret = super.onPrimary(game, action, mods, cursorPos)
        if(ret == GLFW.GLFW_RELEASE && active){
            action(this)
        }
        return ret
    }
}