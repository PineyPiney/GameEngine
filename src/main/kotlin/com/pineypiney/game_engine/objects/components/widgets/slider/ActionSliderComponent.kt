package com.pineypiney.game_engine.objects.components.widgets.slider

import com.pineypiney.game_engine.objects.GameObject

abstract class ActionSliderComponent<T: Number>(
	parent: GameObject,
	value: T,
	val action: (ActionSliderComponent<T>) -> Unit
) : SliderComponent<T>(parent, value) {

	override var value: T
		get() = super.value
		set(value) {
			super.value = value
			action(this)
		}
}