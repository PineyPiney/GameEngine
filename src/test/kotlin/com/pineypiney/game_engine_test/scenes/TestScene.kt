package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.rendering.DefaultWindowGameRenderer
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

abstract class TestScene(override val id: String, override val gameEngine: WindowedGameEngineI<*>) : WindowGameLogic() {

	override val renderer: GameRenderer<TestScene> = DefaultWindowGameRenderer(window, ::OrthographicCamera) { gameEngine.manager.createPresentingApi(window, id) }

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if (action == 1) {
			if (state.i == GLFW_KEY_ESCAPE) {
				window.shouldClose = true
			} else when (state.c) {
				'F' -> toggleFullscreen()
				else -> onPress(state.c)
			}
		}
		return action
	}

	open fun onPress(key: Char) {

	}
}