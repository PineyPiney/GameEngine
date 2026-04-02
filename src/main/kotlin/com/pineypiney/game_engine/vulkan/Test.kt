package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowGameLogic
import org.lwjgl.glfw.GLFW

class Logic(override val gameEngine: VulkanGameEngine<Logic>) : WindowGameLogic() {
	override val renderer: VulkanBufferedRenderer<Logic> =
		VulkanBufferedRenderer<Logic>(window, gameEngine.vulkanManager)

	override fun addObjects() {

	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}
}

fun main() {
	LibrarySetUp.initGLFW()

	val hints = Window.defaultHints + (GLFW.GLFW_CLIENT_API to GLFW.GLFW_NO_API)
	val window = object : Window("Vulkan Window", 1280, 720, false, false, hints) {
		override val input: Inputs = DefaultInput(this)
		override fun init() {
			// Make the window visible
			GLFW.glfwShowWindow(windowHandle)
			super.init()
		}
	}
	window.init()

	val engine = VulkanGameEngine(window, ::Logic)
	engine.run()
}