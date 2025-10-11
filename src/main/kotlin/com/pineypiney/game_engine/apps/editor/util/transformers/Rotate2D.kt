package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class Rotate2D(parent: GameObject, screen: EditorScreen) : Transformer(parent, screen){

	var selected = 0
	var oldRotation = Quat(Float.MAX_VALUE)
	var startingEuler = Vec3(0f)
	var grabbedAngle = -1f

	val red = Vec4(.8f, 0f, 0f, 1f)
	val green = Vec4(0f, .8f, 0f, 1f)
	val blue = Vec4(0f, 0f, .8f, 1f)

	override var forceUpdate: Boolean = true

	override fun startAt(obj: GameObject, screen: EditorScreen) {
		super.startAt(obj, screen)
		parent.rotation = obj.transformComponent.worldRotation
	}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(pressed) return

		val pos = getCursorPos(cursorPos)
		val relX = (pos.x - parent.transformComponent.position.x)/parent.scale.x
		val relY = (pos.y - parent.transformComponent.position.y)/parent.scale.y
		val rad2 = (relX*relX + relY*relY)
		val relRot = Vec2(parent.worldModel.rotationComponent() * Vec4(-relX, relY, 0f, 1f))
		// Hovering over the blue z circle
		if(abs(rad2 - .33f) < .06f){
			red.x = .8f
			green.y = .8f
			blue.z = 1f
		}
		// Hovering over the red x line
		else if(abs(relRot.x) < .08f){
			red.x = 1f
			green.y = .8f
			blue.z = .8f
		}
		// Hovering over the green y line
		else if(abs(relRot.y) < .08f){
			red.x = .8f
			green.y = 1f
			blue.z = .8f
		}
		else {
			red.x = .8f
			green.y = .8f
			blue.z = .8f
		}
	}

	fun getAngle(cursor: CursorPosition) = (getCursorPos(cursor) - Vec2(parent.position)).angle()

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 1) {
			selected = when (1f) {
				red.x -> 1
				green.y -> 2
				blue.z -> {
					startingEuler = parent.rotation.eulerAngles()
					grabbedAngle = getAngle(cursorPos)
					3
				}

				else -> 0
			}
		}
		if(selected > 0) {
			screen.editingObject?.let {
				when (action) {
					1 -> oldRotation = it.rotation
					0 -> if (oldRotation.x != Float.MAX_VALUE) screen.editManager.addEdit(ComponentFieldEdit.rotateEdit(
						it, screen, oldRotation, it.rotation
					))
				}
			}
		}

		return INTERRUPT
	}

	override fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		when(selected){
			0 -> return
			//1 -> parent.translate(Vec3(cursorDelta.x, 0f, 0f))
			//2 -> parent.translate(Vec3(0f, cursorDelta.y, 0f))
			3 -> {
				val newGrabAngle = getAngle(cursorPos)
				parent.rotation = Quat(startingEuler + Vec3(0f, 0f, grabbedAngle - newGrabAngle))
			}
		}
		screen.editingObject?.transformComponent?.worldRotation = parent.rotation
		screen.componentBrowser.refreshField("TransformComponent.rotation")
	}
}