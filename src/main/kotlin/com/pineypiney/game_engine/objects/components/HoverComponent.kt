package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class HoverComponent(parent: GameObject, val onEnter: (HoverComponent) -> Unit, val onExit: (HoverComponent) -> Unit) :
	DefaultInteractorComponent(parent, "HVR") {

	override fun onCursorEnter(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		onEnter(this)
	}

	override fun onCursorExit(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		onExit(this)
	}
}