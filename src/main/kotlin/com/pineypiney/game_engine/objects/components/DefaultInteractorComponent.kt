package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject

open class DefaultInteractorComponent(parent: GameObject, id: String) : Component(parent, id), InteractorComponent {

	override var hover: Boolean = false
	override var pressed: Boolean = false
	override var forceUpdate: Boolean = false
	override var passThrough: Boolean = false


	companion object {
		const val INTERRUPT = InteractorComponent.INTERRUPT
	}
}