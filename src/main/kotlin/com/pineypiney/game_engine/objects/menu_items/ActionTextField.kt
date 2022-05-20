package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.util.input.InputState
import glm_.i
import glm_.vec2.Vec2

open class ActionTextField(origin: Vec2, size: Vec2, val action: (field: TextField, char: Char, input: Int) -> Unit): TextField(origin, size) {
    override fun type(char: Char) {
        super.type(char)
        action(this, char, char.i)
    }

    override fun specialCharacter(bind: InputState) {
        super.specialCharacter(bind)
        if(standard.contains(bind.i)) action(this, bind.c, bind.i)
    }
}