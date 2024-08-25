package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject

open class ActionSliderComponent(
	parent: GameObject,
	low: Float,
	high: Float,
	value: Float,
	val action: (ActionSliderComponent) -> Unit
) : SliderComponent(parent, low, high, value) {

	override var value: Float
		get() = super.value
		set(value) {
			super.value = value
			action(this)
		}
}