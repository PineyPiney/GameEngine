package com.pineypiney.game_engine.window

import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFWGammaRamp
import org.lwjgl.glfw.GLFWVidMode

interface MonitorI {

    /**
     * - [videoMode]: The current Video Mode being used by the monitor
     * - [videoModes]: All Video Modes supported by the monitor
     * - [physicalSize]: The physical size of the Monitor in millimeters
     */
    val handle: Long
    val pos: Vec2i
    val physicalSize: Vec2i
    val contentScale: Vec2
    val name: String?
    val videoModes: Array<GLFWVidMode>
    val videoMode: GLFWVidMode
    var gammaRamp: GLFWGammaRamp
}