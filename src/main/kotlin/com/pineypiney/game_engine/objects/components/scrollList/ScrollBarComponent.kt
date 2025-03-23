package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI

class ScrollBarComponent(parent: GameObject) : DefaultInteractorComponent(parent) {

	override fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onDrag(window, cursorPos, cursorDelta, ray)
		forceUpdate = true
		parent.parent!!.getComponent<ScrollListComponent>()?.onDragBar(window, cursorDelta.position.y, ray)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		val p = super.onPrimary(window, action, mods, cursorPos)

		if (!pressed) forceUpdate = false

		return p
	}
}