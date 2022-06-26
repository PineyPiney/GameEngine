package com.pineypiney.game_engine.util.input

import glm_.bool
import glm_.i
import glm_.s
import kool.ByteBuffer
import kool.lib.forEachIndexed
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState

class GamePad(val id: Int, val inputs: Inputs) {

    val state = GLFWGamepadState(ByteBuffer(40))

    // Joy stick and trigger values
    val axes = FloatArray(6)

    // Button values
    val buttonStates = BooleanArray(15)

    // This function is called every render cycle for every connected gamepad
    fun input(){
        // This function edits state to contain the correct information
        GLFW.glfwGetGamepadState(id, state)

        state.axes().forEachIndexed{ i, f ->
            updateAxes(i, f)
        }

        state.buttons().forEachIndexed { i, b ->
            updateButton(i, b.bool)
        }
    }

    fun updateAxes(axis: Int, state: Float){
        if(axes[axis] == state) return

        // This triggers the input function if the axis is pushed past half way
        if(axes[axis] < 0 && state > 0) inputs.onInput(axis.s, 1, ControlType.GAMEPAD_AXIS)
        else if(axes[axis] > 0 && state < 0) inputs.onInput(axis.s, 0, ControlType.GAMEPAD_AXIS)

        axes[axis] = state
    }

    fun updateButton(button: Int, state: Boolean){
        if(buttonStates[button] == state) return

        buttonStates[button] = state

        inputs.onInput(button.s, state.i, ControlType.GAMEPAD_BUTTON)
    }
}