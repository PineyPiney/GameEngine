package com.pineypiney.game_engine.objects.menu_items.slider

import glm_.vec2.Vec2

class BasicSlider(override var origin: Vec2, size: Vec2, low: Float, high: Float, value: Float): OutlinedSlider(size, low, high, value) {
    override val pointer: SliderPointer = BasicSliderPointer(this, size)
}