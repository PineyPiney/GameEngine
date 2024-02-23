package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.InputState
import glm_.i

open class ActionTextFieldComponent<E: TextFieldComponent>(parent: GameObject, textOffset: Float = 0f, textSize: Float = 1f, val updateType: Int = UPDATE_ON_FINISH, val action: (field: E, char: Char, input: Int) -> Unit): TextFieldComponent(parent, textOffset, textSize) {

    override fun type(char: Char) {
        super.type(char)
        if(updateType == UPDATE_EVERY_CHAR) action(this as E, char, char.i)
    }

    override fun specialCharacter(bind: InputState) {
        super.specialCharacter(bind)
        if(standard.contains(bind.c) && updateType == UPDATE_EVERY_CHAR) action(this as E, bind.c, bind.i)
    }

    override fun finish() {
        super.finish()
        if(updateType == UPDATE_ON_FINISH) action(this as E, ' ', 0)
    }

    companion object{
        const val UPDATE_EVERY_CHAR = 1
        const val UPDATE_ON_FINISH = 2
    }
}