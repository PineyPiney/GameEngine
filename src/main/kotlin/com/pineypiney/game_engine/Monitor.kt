package com.pineypiney.game_engine

import com.pineypiney.game_engine.util.extension_functions.toArray
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kool.FloatBuffer
import kool.IntBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWVidMode
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Monitor(handle: Long) {

    val handle = if(handle == 0L) GLFW.glfwGetPrimaryMonitor() else handle

    /**
     * The current Video Mode being used by the monitor
     */
    val videoMode: GLFWVidMode get() = GLFW.glfwGetVideoMode(handle) ?: throw Exception()

    /**
     * All Video Modes supported by the monitor
     */
    val videoModes: Array<GLFWVidMode> get() = GLFW.glfwGetVideoModes(handle)?.toArray() ?: throw Exception()

    val maxVideoMode: GLFWVidMode get() = videoModes.last()

    /**
     * The physical size of the Monitor in millimeters
     */
    val physicalSize: Vec2i get() = getVec2i(GLFW::glfwGetMonitorPhysicalSize)

    /**
     *
     */
    val pos: Vec2i get() = getVec2i(GLFW::glfwGetMonitorPos)
    val contentScale get() = getVec2(GLFW::glfwGetMonitorContentScale)
    val name get() = GLFW.glfwGetMonitorName(handle)

    fun getVec2i(func: (Long, IntBuffer, IntBuffer) -> Unit): Vec2i {
        val (wa, ha) = arrayOf(IntBuffer(1), IntBuffer(1))
        func(handle, wa, ha)
        return Vec2i(wa[0], ha[0])
    }
    fun getVec2(func: (Long, FloatBuffer, FloatBuffer) -> Unit): Vec2 {
        val (wa, ha) = arrayOf(FloatBuffer(1), FloatBuffer(1))
        func(handle, wa, ha)
        return Vec2(wa[0], ha[0])
    }

    companion object {
        val primary get() = Monitor(GLFW.glfwGetPrimaryMonitor())
        fun getAllMonitors(): LongArray = GLFW.glfwGetMonitors()?.toArray() ?: longArrayOf()
    }
}