package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

abstract class MultiTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<MultiTest, OrthographicCamera>(window, OrthographicCamera(window))
	val camera get() = renderer.camera

	private val pressedKeys = mutableSetOf<Short>()

	var i = 0
	abstract val tests: List<Test>

	override fun addObjects() {

	}

	override fun init() {
		super.init()
		for(test in tests) for(layer in test.objects.map) layer.value.init()
		gameObjects += tests.first().objects
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)

		val speed = 10 * Timer.frameDelta
		val travel = Vec2()

		if(pressedKeys.contains('W'.s)) travel += Vec2(0, speed)
		if(pressedKeys.contains('S'.s)) travel -= Vec2(0, speed)
		if(pressedKeys.contains('A'.s)) travel -= Vec2(speed, 0)
		if(pressedKeys.contains('D'.s)) travel += Vec2(speed, 0)

		if(travel != Vec2(0)){
			camera.translate(travel)
		}
	}

	override fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
		tests[i].onCursorMove(cursorPos, cursorDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
				' ' -> {
					if(tests.size > 1) {
						gameObjects.map.clear()
						i = (i + 1) % tests.size
						gameObjects += tests[i].objects
					}
				}
			}
		}
		tests[i].onInput(state, action)


		if(action == 0) pressedKeys.remove(state.key)
		else pressedKeys.add(state.key)
		return action
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}

	open class Test(val objects: ObjectCollection){
		@Suppress("UNCHECKED_CAST")
		constructor(vararg objects: GameObject): this(ObjectCollection(objects as Array<GameObject>))

		open fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition){}
		open fun onInput(state: InputState, action: Int){}
	}
}