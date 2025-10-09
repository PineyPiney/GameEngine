package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.Arrow2DRenderer
import com.pineypiney.game_engine.objects.components.rendering.collision.Collision3DPolygonRenderer
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Triangle3D
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class Collision3DTest(gameEngine: WindowedGameEngineI<*>): MultiTest(gameEngine) {


	val base = GameObject("Base").apply {
		position = Vec3(0f, -5f, 0f)
		components.add(Collider3DComponent(this, Cuboid(Vec3(-2f, 0f, 0f), Quat(), Vec3(2f))))
		addChild(CollisionBox3DRenderer.create(this, 100f))
	}

	val triangle = Triangle3D(Vec3(-.5f, -.5f, 0f), Vec3(0f, .5f, 0f), Vec3(.5f, -.5f, 0f))

	val dragItem = GameObject("Dragging").apply {
		components.add(Collider3DComponent(this, triangle))
		addChild(Collision3DPolygonRenderer.create(this, 5f))
	}
	val ejectionArrow = GameObject("Ejection Arrow").apply {
		components.add(Arrow2DRenderer(this, Vec2(), Vec2(1f), .2f).apply { visible = false })
	}
	val triaDragTest = object : Test(base, dragItem, ejectionArrow){
		val dragCollider = dragItem.getComponent<Collider3DComponent>()!!
		val baseCollider = base.getComponent<Collider3DComponent>()!!
		val arrowRenderer = ejectionArrow.getComponent<Arrow2DRenderer>()!!
		var startPoint: Vec3? = null
		override fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
			val oldPosition = startPoint ?: dragItem.position
			dragItem.position = Vec3(cursorPos.position * (camera.height * .5f) + Vec2(camera.cameraPos), 0)
			val colliding = dragCollider.transformedShape intersects baseCollider.transformedShape
			dragItem.children.first().getComponent<Collision3DPolygonRenderer>()?.colour(if(colliding) Vec4(1f) else Vec4(0f, 0f, 0f, 1f))
			if(colliding){
				val ejection = dragCollider.transformedShape.getEjectionNew(baseCollider.transformedShape, dragItem.position - oldPosition)
				arrowRenderer.visible = true
				val pos = Vec2(dragItem.position)
				arrowRenderer.setOriginAndPoint(pos, pos + Vec2(ejection))
			}
			else arrowRenderer.visible = false
		}

		override fun onInput(state: InputState, action: Int) {
			if(action == 1 && state.triggers(InputState(GLFW.GLFW_MOUSE_BUTTON_2, ControlType.MOUSE))){
				startPoint = if(startPoint == null) dragItem.position
				else null
			}
		}
	}

	override val tests: List<Test> = listOf(triaDragTest)}