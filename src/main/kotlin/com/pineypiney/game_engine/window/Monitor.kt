package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.GLFunc.Companion.getVec2
import com.pineypiney.game_engine.util.GLFunc.Companion.getVec2i
import com.pineypiney.game_engine.util.extension_functions.toArray
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGammaRamp
import org.lwjgl.glfw.GLFWVidMode

class Monitor(handle: Long): MonitorI {

    override val handle = if(handle == 0L) GLFW.glfwGetPrimaryMonitor() else handle

    override val pos: Vec2i get() = getVec2i(handle, GLFW::glfwGetMonitorPos)

    override val physicalSize: Vec2i get() = getVec2i(handle, GLFW::glfwGetMonitorPhysicalSize)

    override val contentScale get() = getVec2(handle, GLFW::glfwGetMonitorContentScale)

    override val name: String? get() = GLFW.glfwGetMonitorName(handle)

    override val videoModes: Array<GLFWVidMode> get() = GLFW.glfwGetVideoModes(handle)?.toArray() ?: throw Exception()

    override val videoMode: GLFWVidMode get() = GLFW.glfwGetVideoMode(handle) ?: throw Exception()

    override var gammaRamp: GLFWGammaRamp
        get() = GLFW.glfwGetGammaRamp(handle)!!
        set(value) = GLFW.glfwSetGammaRamp(handle, value)

    companion object {
        val primary get() = Monitor(GLFW.glfwGetPrimaryMonitor())
        fun getAllMonitors(): LongArray = GLFW.glfwGetMonitors()?.toArray() ?: longArrayOf()
    }
}