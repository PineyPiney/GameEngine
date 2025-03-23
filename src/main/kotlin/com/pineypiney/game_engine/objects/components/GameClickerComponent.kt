package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI

class GameClickerComponent(parent: GameObject, val primaryClick: () -> Unit = {}, val secondaryClick: () -> Unit = {}) :
	DefaultInteractorComponent(parent) {

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		val p = super.onPrimary(window, action, mods, cursorPos)
		if (action == 1) primaryClick()
		return p
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		val s = super.onSecondary(window, action, mods, cursorPos)
		if (action == 1) secondaryClick()
		return s
	}

	override fun checkHover(ray: Ray, screenPos: CursorPosition): Float {
		return ray.distanceTo(parent.getShape())
	}
}