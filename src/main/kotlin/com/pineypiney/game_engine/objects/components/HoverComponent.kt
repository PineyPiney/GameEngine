package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI

class HoverComponent(parent: GameObject, val onEnter: (HoverComponent) -> Unit, val onExit: (HoverComponent) -> Unit) :
	DefaultInteractorComponent(parent) {

	override fun onCursorEnter(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		onEnter(this)
	}

	override fun onCursorExit(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		onExit(this)
	}
}