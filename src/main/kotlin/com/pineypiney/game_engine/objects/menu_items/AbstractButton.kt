package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent
import glm_.vec2.Vec2

abstract class AbstractButton(name: String) : MenuItem(name) {

	abstract val action: (ButtonComponent, Vec2) -> Unit

	override fun addComponents() {
		super.addComponents()
		components.add(ButtonComponent(this, action))
	}
}