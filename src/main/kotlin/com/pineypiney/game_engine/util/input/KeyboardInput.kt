package com.pineypiney.game_engine.util.input

import glm_.b
import glm_.s
import org.lwjgl.glfw.GLFW.glfwSetCharCallback
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback
import org.lwjgl.glfw.GLFWCharCallback
import org.lwjgl.glfw.GLFWKeyCallback

class KeyboardInput(val input: Inputs, key: GLFWKeyCallback? = null, char: GLFWCharCallback? = null) {

    private val keyCallback = key ?: object : GLFWKeyCallback() {
        override fun invoke(handle: Long, key: Int, scancode: Int, action: Int, mods: Int) {
            input.onInput(key.s, action, mods.b, Inputs.ControlType.KEYBOARD)
        }
    }

    private val charCallback = char ?: object : GLFWCharCallback() {
        override fun invoke(window: Long, codepoint: Int) {
            input.keyboardCharCallback(codepoint)
        }
    }

    init{
        glfwSetKeyCallback(input.window.windowHandle, keyCallback)
        glfwSetCharCallback(input.window.windowHandle, charCallback)
    }
}