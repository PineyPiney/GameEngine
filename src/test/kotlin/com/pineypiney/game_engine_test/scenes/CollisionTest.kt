package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Rigidbody2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.Arrow2DRenderer
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionPolygonRenderer
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Parallelogram
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import kotlin.math.sqrt

class CollisionTest(gameEngine: WindowedGameEngineI<*>): MultiTest(gameEngine) {

	val bases = Array(5){ GameObject("Base$it").apply {
		position = Vec3(2.8f*it -7f, -5f, 0f)
		scale = Vec3(2.8f, 2f, 1f)
		components.addAll(
			Collider2DComponent(this)
		)
		addChild(CollisionBox2DRenderer.create(this))
	} }
	val base = GameObject("Base").apply {
		position = Vec3(-7f, -5f, 0f)
		scale = Vec3(14f, 2f, 1f)
		components.addAll(
			Collider2DComponent(this)
		)
		addChild(CollisionBox2DRenderer.create(this))
	}

	val item = GameObject("Falling").apply {
		components.addAll(
			Collider2DComponent(this),
			Rigidbody2DComponent(this, 1f, 0f, 0f)
		)
		addChild(CollisionBox2DRenderer.create(this))
	}

	val squareTest = object : Test(*bases, item){
		var flip = false
		override fun onInput(state: InputState, action: Int) {
			if(action == 1 && state.triggers(InputState('L'))) {
				if (flip) {
					item.position = Vec3(6f, 0f, 0f)
					item.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(-5f, 6f)
				} else {
					item.position = Vec3(-7f, 0f, 0f)
					item.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(5f, 6f)
				}
				flip = !flip
			}
		}
	}
	val slopes = Array(5){ GameObject("Base$it").apply {
		position = Vec3(5f - sqrt(8f)*it, sqrt(8f)*it - 5f, 0f)
		rotation = Quat(Vec3(0f, 0f, PIF * -.25f))
		scale = Vec3(4f, 2f, 1f)
		components.addAll(
			Collider2DComponent(this)
		)
		addChild(CollisionBox2DRenderer.create(this))
	} }
	val slope = GameObject("Base").apply {
		position = Vec3(-7f, -5f, 0f)
		rotation = Quat(Vec3(0f, 0f, PIF * .25f))
		scale = Vec3(14f, 2f, 1f)
		components.addAll(
			Collider2DComponent(this)
		)
		addChild(CollisionBox2DRenderer.create(this))
	}

	val slopeItem = GameObject("Falling").apply {
		rotation = Quat(Vec3(0f, 0f, PIF * .25f))
		components.addAll(
			Collider2DComponent(this),
			Rigidbody2DComponent(this, 1f, 0f, 0f)
		)
		addChild(CollisionBox2DRenderer.create(this))
	}

	val slopeTest = object : Test(*slopes, slopeItem){
		var flip = false
		override fun onInput(state: InputState, action: Int) {
			if(action == 1 && state.triggers(InputState('L'))) {
				if (flip) {
					slopeItem.position = Vec3(6f, 0f, 0f)
					slopeItem.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(-5f, 6f)
				} else {
					slopeItem.position = Vec3(-7f, 0f, 0f)
					slopeItem.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(5f, 6f)
				}
				flip = !flip
			}
		}
	}

	val paraBase = GameObject("Base").apply {
		position = Vec3(0f, -5f, 0f)
		components.addAll(
			Collider2DComponent(this, Parallelogram(Vec2(0f), Vec2(-8f, 2f), Vec2(8f, 2f)))
		)
		addChild(CollisionPolygonRenderer.create(this, 100f))
	}

	val paraItem = GameObject("Falling").apply {
		position = Vec3(.1f, 3f, 0f)
		components.addAll(
			Collider2DComponent(this, Parallelogram(Vec2(0f, -.5f), Vec2(-1f, .5f), Vec2(1f, .5f))),
			Rigidbody2DComponent(this).apply { dragCoefficient = 0f; friction = 0f }
		)
		addChild(CollisionPolygonRenderer.create(this, 5f))
	}

	val parallelogramTest = object : Test(paraBase, paraItem){
		override fun onInput(state: InputState, action: Int) {
			if(action == 1 && state.triggers(InputState('L'))){
				paraItem.position = Vec3(1f, 4f, 0f)
				paraItem.getComponent<Rigidbody2DComponent>()?.velocity = Vec2(0f)
			}
		}
	}

	val dragBase = GameObject("Base").apply {
		position = Vec3(0f, -5f, 0f)
		components.add(Collider2DComponent(this, Parallelogram(Vec2(0f), Vec2(-8f, 2f), Vec2(8f, 2f))))
		addChild(CollisionPolygonRenderer.create(this, 100f))
	}

	val dragItem = GameObject("Dragging").apply {
		components.add(Collider2DComponent(this, Parallelogram(Vec2(0f, -.5f), Vec2(-1f, .5f), Vec2(1f, .5f))),)
		addChild(CollisionPolygonRenderer.create(this, 5f))
	}
	val ejectionArrow = GameObject("Ejection Arrow").apply {
		components.add(Arrow2DRenderer(this, Vec2(), Vec2(1f), .2f).apply { visible = false })
	}
	val paraDragTest = object : Test(dragBase, dragItem, ejectionArrow){
		val dragCollider = dragItem.getComponent<Collider2DComponent>()!!
		val baseCollider = dragBase.getComponent<Collider2DComponent>()!!
		val arrowRenderer = ejectionArrow.getComponent<Arrow2DRenderer>()!!
		var startPoint: Vec2? = null
		override fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
			val oldPosition = startPoint ?: Vec2(dragItem.position)
			dragItem.position = Vec3(cursorPos.position * (camera.height * .5f) + Vec2(camera.cameraPos), 0)
			val colliding = dragCollider.transformedShape intersects baseCollider.transformedShape
			dragItem.children.first().getComponent<CollisionPolygonRenderer>()?.colour(if(colliding) Vec4(1f) else Vec4(0f, 0f, 0f, 1f))
			if(colliding){
				val ejection = dragCollider.transformedShape.getEjectionNew(baseCollider.transformedShape, Vec2(dragItem.position) - oldPosition)
				arrowRenderer.visible = true
				val pos = Vec2(dragItem.position)
				arrowRenderer.setOriginAndPoint(pos, pos + ejection)
			}
			else arrowRenderer.visible = false
		}

		override fun onInput(state: InputState, action: Int) {
			if(action == 1 && state.triggers(InputState(GLFW.GLFW_MOUSE_BUTTON_2, ControlType.MOUSE))){
				startPoint = if(startPoint == null) Vec2(dragItem.position)
				else null
			}
		}
	}

	override val tests: List<Test> = listOf(squareTest, slopeTest, parallelogramTest, paraDragTest)}