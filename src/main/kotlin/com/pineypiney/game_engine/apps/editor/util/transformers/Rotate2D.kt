package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
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

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(pressed) return

		val relX = cursorPos.x - parent.transformComponent.position.x
		val relY = cursorPos.y - parent.transformComponent.position.y
		val rad2 = (relX*relX + relY*relY) * 11.1111f

		val relRot = Vec2(parent.worldModel.rotationComponent() * Vec4(-relX, relY, 0f, 1f))
		if(abs(rad2 - .23f) < .02f){
			red.x = .8f
			green.y = .8f
			blue.z = 1f
		}
		else if(abs(relRot.x) < .02f){
			red.x = 1f
			green.y = .8f
			blue.z = .8f
		}
		else if(abs(relRot.y) < .02f){
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

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 1) {
			selected = when (1f) {
				red.x -> 1
				green.y -> 2
				blue.z -> {
					startingEuler = parent.rotation.eulerAngles()
					grabbedAngle = (cursorPos - Vec2(parent.position)).angle()
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

		return action
	}

	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		when(selected){
			0 -> return
			//1 -> parent.translate(Vec3(cursorDelta.x, 0f, 0f))
			//2 -> parent.translate(Vec3(0f, cursorDelta.y, 0f))
			3 -> {
				val newGrabAngle = (cursorPos - Vec2(parent.position)).angle()
				parent.rotation = Quat(startingEuler + Vec3(0f, 0f, grabbedAngle - newGrabAngle))
			}
		}
		screen.editingObject?.transformComponent?.worldRotation = parent.rotation
		screen.componentBrowser.refreshField("TransformComponent.rotation")
	}
}