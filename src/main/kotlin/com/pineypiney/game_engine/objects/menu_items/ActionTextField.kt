package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowI
import glm_.i
import glm_.vec2.Vec2

open class ActionTextField(origin: Vec2, size: Vec2, window: WindowI, val action: (field: TextField, char: Char, input: Int) -> Unit): TextField(origin, size, window) {
    override fun type(char: Char) {
        super.type(char)
        action(this, char, char.i)
    }

    override fun specialCharacter(bind: InputState) {
        super.specialCharacter(bind)
        if(standard.contains(bind.i)) action(this, bind.c, bind.i)
    }
}