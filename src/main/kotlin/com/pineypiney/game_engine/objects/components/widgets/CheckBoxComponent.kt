package com.pineypiney.game_engine.objects.components.widgets

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.window.WindowI

class CheckBoxComponent(parent: GameObject, val action: (Boolean) -> Unit) : DefaultInteractorComponent(parent) {

	var ticked = false

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		if (action == 1) toggle()
		return super.onPrimary(window, action, mods, cursorPos)
	}

	fun toggle() {
		ticked = !ticked
		action(ticked)
	}
}