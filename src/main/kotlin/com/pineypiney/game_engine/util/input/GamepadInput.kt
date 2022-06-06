package com.pineypiney.game_engine.util.input

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWJoystickCallback
import org.lwjgl.glfw.GLFWJoystickCallbackI

open class GamepadInput(val input: Inputs, key: GLFWJoystickCallbackI? = null) {

    // This callback is called whenever a gamepad is connected
    private val joystickCallback = key ?: object : GLFWJoystickCallback() {
        override fun invoke(jid: Int, event: Int) {
            when(event){
                GLFW_CONNECTED -> connectedGamepads.add(GamePad(jid, input))
                GLFW_DISCONNECTED -> connectedGamepads.removeIf{ it.id == jid }
            }
        }

    }

    // A list of all connected gamepads
    val connectedGamepads = mutableSetOf<GamePad>()

    init{
        glfwSetJoystickCallback(joystickCallback)

        // GLFW supports up to 16 connected gamepads, at the beginning iterate through each and check if it is connected
        for(i in 0..15){
            if(glfwJoystickPresent(i)) connectedGamepads.add(GamePad(i, input))
        }
    }

    // Because GLFW doesn't have a callback for gamepad inputs they must be surveyed every render loop
    fun input(){
        for(gamepad in connectedGamepads) gamepad.input()
    }
}