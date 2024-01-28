package com.pineypiney.game_engine.level_editor.util

import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.and

class KeyBind(val bind: InputState) {

    constructor(key: Int, controlType: ControlType = ControlType.KEYBOARD, mods: Byte = 0): this(InputState(key, controlType, mods))

    var state: Int = 0

    val key; get() = bind.key
    val controlType; get() = bind.controlType
    val mods; get() = bind.mods

    infix fun activatedBy(state: InputState): Boolean{
        return state.key == this.key &&
                state.controlType == this.controlType &&
                matchMods(state.mods)
    }

    private fun matchMods(mods: Byte): Boolean{
        return (!bind.shift || mods and 1 > 0) &&
                (!bind.control || mods and 2 > 0) &&
                (!bind.alt || mods and 4 > 0) &&
                (!bind.super_ || mods and 8 > 0) &&
                (!bind.caps || mods and 16 > 0) &&
                (!bind.num || mods and 32 > 0)
    }
}