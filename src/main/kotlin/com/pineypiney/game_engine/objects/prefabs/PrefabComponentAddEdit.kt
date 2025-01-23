package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Components

class PrefabComponentAddEdit(parentLoc: String, val component: String) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val c = findDescendant(obj) ?: return
		Components.createComponent(component, c)
	}
}