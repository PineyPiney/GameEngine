package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.input.gamepads.GamepadInput
import com.pineypiney.game_engine.util.input.knm.KeyboardInput
import com.pineypiney.game_engine.util.input.knm.MouseInput
import glm_.c
import org.lwjgl.glfw.GLFW

abstract class WindowedGameEngine<E : WindowGameLogic>(final override val manager: ApiManager, override val resourcesLoader: ResourcesLoader = ResourcesLoader.directoryAndEngine(manager.createResourceFactory())) :
	GameEngine<E>(), WindowedGameEngineI<E> {

	override val input; get() = window.input

	override fun init() {
		manager.init()
		super.init()

		activeScreen.open()
		window.setFrameBufferResizeCallback { activeScreen.updateAspectRatio() }

		setInputCallbacks()
	}

	override fun gameLoop() {
		super.gameLoop()

		if (!window.vSync) {
			// sync means that the game only runs game loops at the intended FPS
			sync()
		}
	}

	override fun setInputCallbacks() {
		input.getInput<MouseInput>().cursorMoveCallback = { screenPos, cursorOffset ->
			activeScreen.onCursorMove(screenPos, cursorOffset)
		}
		input.getInput<MouseInput>().mouseScrollCallback = { scrollOffset ->
			activeScreen.onScroll(scrollOffset)
		}
		input.getInput<MouseInput>().mouseButtonCallback = { bind, action ->
			activeScreen.onInput(bind, action)
		}
		input.getInput<KeyboardInput>().keyCallback = { bind, action ->
			activeScreen.onInput(bind, action)
		}
		input.getInput<KeyboardInput>().charCallback = { codepoint ->
			activeScreen.onType(codepoint.c)
		}
		input.getInputOrNull<GamepadInput>()?.gamepadButtonCallback = { input, action ->
			activeScreen.onInput(input, action)
		}
	}

	override fun update(interval: Float) {
		super.update(interval)
		activeScreen.update(interval, input)
	}

	override fun render(tickDelta: Double) {
		activeScreen.render(tickDelta)
		window.update()
	}

	override fun input() {
		input.input()
	}

	override fun shouldRun(): Boolean = !window.shouldClose

	override fun cleanUp() {
		manager.waitIdle()
		super.cleanUp()
		GLFW.glfwTerminate()
		manager.delete()
	}
}