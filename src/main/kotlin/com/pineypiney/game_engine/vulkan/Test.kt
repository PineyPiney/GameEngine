package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowGameLogic
import org.lwjgl.glfw.GLFW

class Logic(override val gameEngine: VulkanGameEngine<Logic>) : WindowGameLogic() {
	override val renderer: VulkanBufferedRenderer<Logic> = VulkanBufferedRenderer<Logic>(window, gameEngine)

	override fun addObjects() {
		add(renderer.movement.parent)
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT
		else if (action == 1 && state.triggers(InputState(GLFW.GLFW_KEY_ESCAPE))) {
			window.shouldClose = true
			return action
		}
		return action
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

	val engine = VulkanGameEngine(window, VulkanManager(), ::Logic)
	engine.run()
}