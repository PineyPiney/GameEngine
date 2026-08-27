package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

abstract class TestScene(override val gameEngine: WindowedGameEngineI<*>, override val renderer: WindowRendererI<TestScene>) : WindowGameLogic() {

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