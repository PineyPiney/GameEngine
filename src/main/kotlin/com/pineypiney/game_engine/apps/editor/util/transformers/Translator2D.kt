package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.EditorPositioningComponent
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class 	Translator2D(parent: GameObject, screen: EditorScreen) : Transformer(parent, screen){

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

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(pressed) return

		val relX = cursorPos.x - parent.transformComponent.position.x
		val relY = cursorPos.y - parent.transformComponent.position.y
		if(abs(relY) < .012f && relX > 0f){
			red.x = 1f
			green.y = .8f
			blue.z = .8f
		}
		else if(abs(relX) < .012f && relY > 0f){
			red.x = .8f
			green.y = 1f
			blue.z = .8f
		}
		else if(relX > 0f && relX < .1f && relY > 0f && relY < .1f){
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

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 1) {
			selected = when (1f) {
				red.x -> 1
				green.y -> 2
				blue.z -> 3
				else -> 0
			}

			if(selected > 0) grabPoint = cursorPos - Vec2(parent.position)
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

	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		val dragPoint = cursorPos - grabPoint
		when(selected){
			0 -> return
			1 -> parent.position = Vec3(dragPoint.x, parent.position.y, parent.position.z)
			2 -> parent.position = Vec3(parent.position.x, dragPoint.y, parent.position.z)
			3 -> parent.position = Vec3(dragPoint, parent.position.z)
		}
		screen.editingObject?.let {
			val placingComponent = it.getComponent<EditorPositioningComponent>()
			var newWorldPos = Vec3(Vec2(screen.renderer.camera.screenToWorld(Vec2(parent.position.x / window.aspectRatio, parent.position.y))), screen.editingObject!!.position.z)
			if(placingComponent != null) {
				newWorldPos = placingComponent.place(it.transformComponent.worldPosition, newWorldPos)
				screen.repositionTransformer()
			}
			it.transformComponent.worldPosition = newWorldPos
			screen.componentBrowser.refreshField("TransformComponent.position")
		}
	}
}