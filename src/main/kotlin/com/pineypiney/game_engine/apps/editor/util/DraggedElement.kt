package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent

class DraggedElement(parent: GameObject, val element: Draggable): DefaultInteractorComponent(parent) {

	var isDroppable = false
	var hoveringScene = false

	override fun init() {
		super.init()
		forceUpdate = true
		passThrough = true
	}
}