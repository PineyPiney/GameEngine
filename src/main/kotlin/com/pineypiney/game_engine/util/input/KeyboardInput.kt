package com.pineypiney.game_engine.util.input

import glm_.b
import glm_.s
import org.lwjgl.glfw.GLFW.glfwSetCharCallback
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback

open class KeyboardInput(val input: Inputs) {

    private val keyCallback = { _: Long, key: Int, _: Int, action: Int, mods: Int ->
        input.onInput(key.s, action, ControlType.KEYBOARD, mods.b)
    }
    private val charCallback = { _: Long, char: Int ->
        input.keyboardCharCallback(char)
    }

    init{
        glfwSetKeyCallback(input.window.windowHandle, keyCallback)
        glfwSetCharCallback(input.window.windowHandle, charCallback)
    }
}