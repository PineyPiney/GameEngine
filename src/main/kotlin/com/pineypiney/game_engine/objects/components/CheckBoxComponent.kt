package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class CheckBoxComponent(parent: GameObject, val action: (Boolean) -> Unit): InteractorComponent(parent, "CBX") {

    var ticked = false

    override val fields: Array<Field<*>>
        get() = super.fields + BooleanField("tkd", ::ticked){ ticked = it }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        if(action == 1) toggle()
        return super.onPrimary(window, action, mods, cursorPos)
    }

    fun toggle() {
        ticked = !ticked
        action(ticked)
    }
}