package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class ScrollBarComponent(parent: GameObject) : DefaultInteractorComponent(parent) {

	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onDrag(window, cursorPos, cursorDelta, ray)
		forceUpdate = true
		parent.parent!!.getComponent<ScrollListComponent>()?.onDragBar(window, cursorDelta.y, ray)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		val p = super.onPrimary(window, action, mods, cursorPos)

		if (!pressed) forceUpdate = false

		return p
	}
}