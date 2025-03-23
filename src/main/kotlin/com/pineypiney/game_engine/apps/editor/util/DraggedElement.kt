package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class DraggedElement(parent: GameObject, val element: Any, val position: (GameObject, Vec2) -> Unit): DefaultInteractorComponent(parent) {

	var isDroppable = false

	override fun init() {
		super.init()
		forceUpdate = true
		passThrough = true
	}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		position(parent, cursorPos.position)
	}
}