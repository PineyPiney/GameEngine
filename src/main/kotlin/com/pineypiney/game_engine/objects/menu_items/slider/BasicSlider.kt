package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.slider.FloatSliderComponent
import com.pineypiney.game_engine.objects.components.slider.OutlinedSliderRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import glm_.vec2.Vec2

class BasicSlider(origin: Vec2, size: Vec2, low: Float, high: Float, value: Float) : MenuItem() {

	init {
		os(origin, size)
		components.add(FloatSliderComponent(this, low, high, value))
		components.add(OutlinedSliderRendererComponent(this))
	}
}