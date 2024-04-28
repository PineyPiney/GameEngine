package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class ButtonComponent(parent: GameObject, val action: (button: ButtonComponent) -> Unit): DefaultInteractorComponent(parent, "BTN"){

    var active: Boolean = true

    override val fields: Array<Field<*>> = super.fields + BooleanField("act", ::active) { active = it }

    override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
        super.onInput(window, input, action, cursorPos)
        if(action == 1 && input == InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) && active){
            action(this)
        }
        return action
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val ret = super.onPrimary(window, action, mods, cursorPos)
        if(ret == GLFW.GLFW_RELEASE && active){
            action(this)
        }
        return ret
    }
}