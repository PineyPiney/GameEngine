package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.slider.ActionSliderComponent
import com.pineypiney.game_engine.objects.components.slider.OutlinedSliderRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import glm_.vec2.Vec2

class BasicActionSlider(
	origin: Vec2,
	size: Vec2,
	low: Float,
	high: Float,
	value: Float,
	val action: (ActionSliderComponent) -> Unit
) : MenuItem() {

	init {
		os(origin, size)
		components.add(ActionSliderComponent(this, low, high, value, action))
		components.add(OutlinedSliderRendererComponent(this))
	}
}