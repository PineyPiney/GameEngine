package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class ClickerComponent(parent: GameObject, val primaryClick: () -> Unit = {}, val secondaryClick: () -> Unit = {}): InteractorComponent(parent, "CLK") {

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val p = super.onPrimary(window, action, mods, cursorPos)
        if(action == 1) primaryClick()
        return p
    }

    override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val s = super.onSecondary(window, action, mods, cursorPos)
        if(action == 1) secondaryClick()
        return s
    }
}