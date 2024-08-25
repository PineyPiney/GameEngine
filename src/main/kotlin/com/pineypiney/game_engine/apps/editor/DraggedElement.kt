package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class DraggedElement(parent: GameObject, val element: Any): DefaultInteractorComponent(parent, "DEM") {

	override fun init() {
		super.init()
		forceUpdate = true
		passThrough = true
	}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		parent.position = Vec3(cursorPos, parent.position.z)
	}
}