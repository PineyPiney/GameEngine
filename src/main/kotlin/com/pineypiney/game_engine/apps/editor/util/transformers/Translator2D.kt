package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.EditorPositioningComponent
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class Translator2D(parent: GameObject, screen: EditorScreen) : Transformer(parent, screen){

	val xArrow by lazy {
		parent.getChild("X Arrow")?.getComponent<ColouredSpriteComponent>()
	}
	val yArrow by lazy {
		parent.getChild("Y Arrow")?.getComponent<ColouredSpriteComponent>()
	}
	val box by lazy{
		parent.getChild("Box")?.getComponent<ColouredSpriteComponent>()
	}


	var grabPoint = Vec2(-1f)
	var oldPos = Vec3(Float.MAX_VALUE)
	var selected = 0

	val red = Vec4(.8f, 0f, 0f, 1f)
	val green = Vec4(0f, .8f, 0f, 1f)
	val blue = Vec4(0f, 0f, .8f, 1f)

	override var forceUpdate: Boolean = true

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(pressed) return

		val relX = (cursorPos.position.x - parent.position.x) / parent.scale.x
		val relY = (cursorPos.position.y - parent.position.y) / parent.scale.y
		// Hovering over the red Y Arrow
		if(abs(relY) < .06f && relX > 0f){
			red.x = 1f
			green.y = .8f
			blue.z = .8f
		}
		// Hovering over the green X Arrow
		else if(abs(relX) < .06f && relY > 0f){
			red.x = .8f
			green.y = 1f
			blue.z = .8f
		}
		// Hovering over the blue XY Box
		else if(relX > 0f && relX < .5f && relY > 0f && relY < .5f){
			red.x = .8f
			green.y = .8f
			blue.z = 1f
		}
		else {
			red.x = .8f
			green.y = .8f
			blue.z = .8f
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 1) {
			selected = when (1f) {
				red.x -> 1
				green.y -> 2
				blue.z -> 3
				else -> 0
			}

			parent.getComponent<PixelTransformComponent>()?.let { if(selected > 0) grabPoint = cursorPos.screenSpace - it.origin }
		}

		if(selected > 0) {
			screen.editingObject?.let {
				when (action) {
					1 -> oldPos = it.position
					0 -> if (oldPos.x != Float.MAX_VALUE) screen.editManager.addEdit(ComponentFieldEdit.moveEdit(
						it, screen, oldPos, it.position
					))
				}
			}
			return INTERRUPT
		}

		return action
	}

	override fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		val dragPoint = cursorPos.screenSpace - grabPoint
		val transform = transform ?: return

		// Move this gizmo's position on the screen
		when(selected){
			0 -> return
			1 -> transform.origin = Vec2(dragPoint.x, transform.origin.y)
			2 -> transform.origin = Vec2(transform.origin.x, dragPoint.y)
			3 -> transform.origin = dragPoint
		}


		// Move the object being edited to be inline with the gizmo's new position
		screen.editingObject?.let {
			val placingComponent = it.getComponent<EditorPositioningComponent>()
			var newWorldPos = Vec3(Vec2(screen.renderer.camera.screenToWorld(transform.origin)), screen.editingObject!!.position.z)
			if(placingComponent != null) {
				newWorldPos = placingComponent.place(it.transformComponent.worldPosition, newWorldPos)
				screen.repositionTransformer()
			}
			it.transformComponent.worldPosition = newWorldPos
			screen.componentBrowser.refreshField("TransformComponent.position")
		}
	}
}