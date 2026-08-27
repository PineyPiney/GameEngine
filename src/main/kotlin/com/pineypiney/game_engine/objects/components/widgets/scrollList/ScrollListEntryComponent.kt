package com.pineypiney.game_engine.objects.components.widgets.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import glm_.vec3.Vec3

abstract class ScrollListEntryComponent(parent: GameObject) : Component(parent) {

	open val list: ScrollListComponent get() = parent.parent!!.parent!!.getComponent<ScrollListComponent>()!!

	override fun init() {
		super.init()
		parent.scale = Vec3(1f, list.entryHeight, 1f)
	}
}