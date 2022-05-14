package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.Window
import glm_.b
import glm_.s
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW.*

class Inputs(val window: Window) {

    val keyboard = KeyboardInput(this)
    val mouse = MouseInput(this)

    var mouseMoveCallback = { _: Window, _: Vec2, _: Vec2 -> }           // gameEngine.activeScreen.onCursorMove(win, screenPos, cursorOffset)
    var mouseScrollCallback = { _: Window, _: Vec2 -> }                  // gameEngine.activeScreen.onScroll(Window.getWindow(handle), scrollOffset)
    var keyPressCallback = { _: KeyBind, _: Int -> }                     // gameEngine.activeScreen.onInput(input, action)
    var keyboardCharCallback = { _: Int -> }

    val primary = KeyBind(GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)
    val secondary = KeyBind(GLFW_MOUSE_BUTTON_2, ControlType.MOUSE)

    private val modStates: MutableMap<Short, Boolean> = mutableMapOf(
        Pair(GLFW_KEY_LEFT_SHIFT.s, false),
        Pair(GLFW_KEY_LEFT_CONTROL.s, false),
        Pair(GLFW_KEY_LEFT_ALT.s, false),
        Pair(GLFW_KEY_LEFT_SUPER.s, false),
        Pair(GLFW_KEY_CAPS_LOCK.s, false),
        Pair(GLFW_KEY_NUM_LOCK.s, false),
    )

    fun onInput(key: Short, action: Int, mods: Byte, type: ControlType){
        // First check if the key is a mod key and if so update the mods map
        if(modStates.containsKey(key.s)) modStates[key.s] = action > 0

        val input = KeyBind(key, type, mods)

        keyPressCallback(input, action)
    }

    fun getMod(mod: Int) = modStates[mod.s] ?: false
    fun getMods() = modsToByte(
        getMod(GLFW_KEY_LEFT_SHIFT), getMod(GLFW_KEY_LEFT_CONTROL), getMod(GLFW_KEY_LEFT_ALT),
        getMod(GLFW_KEY_LEFT_SUPER), getMod(GLFW_KEY_CAPS_LOCK), getMod(GLFW_KEY_NUM_LOCK))

    fun modsToByte(shift: Boolean, control: Boolean, alt: Boolean, super_: Boolean, caps: Boolean, num: Boolean): Byte
        = (shift.b * 1 + control.b * 2 + alt.b * 4 + super_.b * 8 + caps.b * 16  + num.b * 32).b

    fun setCursorAt(pos: Vec2, drag: Boolean = false){
        this.mouse.setCursorAt(pos, drag)
    }

    enum class ControlType{
        KEYBOARD,
        MOUSE,
        JOYSTICK,
        GAMEPAD_BUTTON,
        GAMEPAD_AXIS
    }
}