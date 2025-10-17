package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.widgets.slider.ActionFloatSliderComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.ActionIntSliderComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.ActionSliderComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.OutlinedSliderRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class BasicActionSlider(name: String): MenuItem(name) {

	constructor(name: String, low: Float, high: Float, value: Float, action: (ActionSliderComponent<Float>) -> Unit): this(name){
		components.add(ActionFloatSliderComponent(this, low, high, value, action))
	}

	constructor(name: String, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): this(name){
		components.add(ActionIntSliderComponent(this, low, high, value, action))
	}

	constructor(name: String, origin: Vec2, size: Vec2, low: Float, high: Float, value: Float, action: (ActionSliderComponent<Float>) -> Unit): this(name, low, high, value, action){
		os(origin, size)
	}

	constructor(name: String, origin: Vec2, size: Vec2, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): this(name, low, high, value, action){
		os(origin, size)
	}

	constructor(name: String, pos: Vec2i, size: Vec2i, origin: Vec2, low: Float, high: Float, value: Float, action: (ActionSliderComponent<Float>) -> Unit): this(name, low, high, value, action){
		pixel(pos, size, origin)
	}

	constructor(name: String, pos: Vec2i, size: Vec2i, origin: Vec2, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): this(name, low, high, value, action){
		pixel(pos, size, origin)
	}

	init {
		components.add(OutlinedSliderRendererComponent(this))
	}
}