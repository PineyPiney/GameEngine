package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.Rigidbody2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

class CollisionTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<CollisionTest, OrthographicCamera>(window, OrthographicCamera(window))
	val camera get() = renderer.camera

	private val pressedKeys = mutableSetOf<Short>()

	val bases = Array(5){ GameObject("Base$it").apply {
		position = Vec3(2.8f*it -7f, -5f, 0f)
		scale = Vec3(2.8f, 2f, 1f)
		components.addAll(
			Collider2DComponent(this)
		)
		addChild(CollisionBox2DRenderer(this))
	} }
	val base = GameObject("Base").apply {
		position = Vec3(-7f, -5f, 0f)
		scale = Vec3(14f, 2f, 1f)
		components.addAll(
			Collider2DComponent(this)
		)
		addChild(CollisionBox2DRenderer(this))
	}

	val item = GameObject("Falling").apply {
		components.addAll(
			Collider2DComponent(this),
			Rigidbody2DComponent(this)
		)
		addChild(CollisionBox2DRenderer(this))
	}
	var flip = false

	override fun addObjects() {
		add(item, *bases)
	}

	override fun init() {
		super.init()
		reset()
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

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
				' ' -> reset()
			}
		}


		if(action == 0) pressedKeys.remove(state.key)
		else pressedKeys.add(state.key)
		return action
	}

	fun reset(){
		if(flip) {
			item.position = Vec3(6f, 0f, 0f)
			item.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(-5f, 6f)
		}
		else{
			item.position = Vec3(-7f, 0f, 0f)
			item.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(5f, 6f)
		}
		flip = !flip
	}

	override fun updateAspectRatio(window: WindowI) {
		super.updateAspectRatio(window)
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}
}