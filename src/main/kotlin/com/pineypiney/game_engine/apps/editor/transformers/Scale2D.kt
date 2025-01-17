package com.pineypiney.game_engine.apps.editor.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class Scale2D(parent: GameObject, screen: EditorScreen) : Transformer(parent, screen){

	var initScale = Vec2(1f)
	var grabPoint = Vec2(0f)
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
		else if(relX > .1f && relX < .2f && relY > .1f && relY < .2f){
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
		selected = when(1f){
			red.x -> 1
			green.y -> 2
			blue.z -> 3
			else -> 0
		}
		if(selected > 0) {
			screen.editingObject ?.let {
				initScale = Vec2(it.scale)
			}
			grabPoint = (cursorPos - Vec2(parent.transformComponent.position))
			if(grabPoint.x == 0f) grabPoint.x = .001f
			if(grabPoint.y == 0f) grabPoint.y = .001f
			grabPoint = Vec2(1f / grabPoint.x, 1f / grabPoint.y)
		}
		return action
	}

	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		val scale = when(selected){
			1 -> Vec2(initScale.x * (cursorPos.x - parent.position.x) * grabPoint.x, initScale.y)
			2 -> Vec2(initScale.x, initScale.y * (cursorPos.y - parent.position.y) * grabPoint.y)
			3 -> initScale * (cursorPos - Vec2(parent.position)) * grabPoint
			else -> return
		}
		//val newWorldPos = Vec3(Vec2(screen.renderer.camera.screenToWorld(Vec2(parent.position.x / window.aspectRatio, parent.position.y))), screen.editingObject!!.position.z)
		screen.editingObject?.transformComponent?.let { it.scale = Vec3(scale, it.scale.z) }
		screen.componentBrowser.refreshField("T2D.scl")
	}
}