package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.NativeFuncs.getVec2
import com.pineypiney.game_engine.util.NativeFuncs.getVec2i
import glm_.vec2.Vec2i
import kool.map
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGammaRamp

class GLFWMonitor(handle: Long) : Monitor {

	val handle = if (handle == 0L) GLFW.glfwGetPrimaryMonitor() else handle

	override val pos: Vec2i get() = getVec2i(handle, GLFW::glfwGetMonitorPos)

	/**
	 * [physicalSize]: The physical size of the Monitor in millimeters
	 */
	val physicalSize: Vec2i get() = getVec2i(handle, GLFW::glfwGetMonitorPhysicalSize)

	override val contentScale get() = getVec2(handle, GLFW::glfwGetMonitorContentScale)

	override val name: String? get() = GLFW.glfwGetMonitorName(handle)

	override val videoModes: Iterable<GLFWVideoMode> get() = GLFW.glfwGetVideoModes(handle)?.map(::GLFWVideoMode) ?: throw Exception()

	override val videoMode: GLFWVideoMode get() = GLFWVideoMode(GLFW.glfwGetVideoMode(handle) ?: throw Exception())

	var gammaRamp: GLFWGammaRamp
		get() = GLFW.glfwGetGammaRamp(handle)!!
		set(value) = GLFW.glfwSetGammaRamp(handle, value)

	companion object {
		val primary get() = GLFWMonitor(GLFW.glfwGetPrimaryMonitor())
		fun getAllMonitors(): List<GLFWMonitor> = GLFW.glfwGetMonitors()?.map(::GLFWMonitor) ?: emptyList()
	}
}