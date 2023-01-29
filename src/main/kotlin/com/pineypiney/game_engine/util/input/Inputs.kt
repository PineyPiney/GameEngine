package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.WindowI
import glm_.and
import glm_.b
import glm_.i
import glm_.pow
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW.*

abstract class Inputs(val window: WindowI) {

    abstract val keyboard: KeyboardInput
    abstract val mouse: MouseInput
    abstract val gamepad: GamepadInput

    var mouseMoveCallback = { _: Vec2, _: Vec2 -> }          // gameEngine.activeScreen.onCursorMove(win, screenPos, cursorOffset)
    var mouseScrollCallback = { _: Vec2 -> }                 // gameEngine.activeScreen.onScroll(Window.getWindow(handle), scrollOffset)
    var keyPressCallback = { _: InputState, _: Int -> }                 // gameEngine.activeScreen.onInput(input, action)
    var keyboardCharCallback = { _: Int -> }                            // gameEngine.activeScreen.onType(input)

    open val primary = InputState(GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)
    open val secondary = InputState(GLFW_MOUSE_BUTTON_2, ControlType.MOUSE)

    private val modStates: MutableMap<Byte, Boolean> = mutableMapOf(
        Pair(GLFW_MOD_SHIFT.b, false),
        Pair(GLFW_MOD_CONTROL.b, false),
        Pair(GLFW_MOD_ALT.b, false),
        Pair(GLFW_MOD_SUPER.b, false),
        Pair(GLFW_MOD_CAPS_LOCK.b, false),
        Pair(GLFW_MOD_NUM_LOCK.b, false),
    )

    open fun input(){
        gamepad.input()
        mouse.update(Timer.frameTime)
    }

    open fun onInput(key: Short, action: Int, type: ControlType, mods: Byte? = null){
        // First check if the key is a mod key and if so update the mods map
        mods?.let { setMods(it) }

        val input = InputState(key, type, mods ?: getMods())

        keyPressCallback(input, action)
    }

    fun getMod(mod: Number) = modStates[mod.b] ?: false
    fun getMods() = modStates.entries.sumOf { (k, v)-> k * v.i }.b

    fun setMods(mods: Byte){
        for(i in 0 .. 5){
            val mod = 2 pow i
            modStates[mod.b] = mods and mod > 0
        }
    }
}