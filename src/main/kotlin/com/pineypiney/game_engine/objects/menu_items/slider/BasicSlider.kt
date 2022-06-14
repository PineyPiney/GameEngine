package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import glm_.vec2.Vec2

class BasicSlider(override var origin: Vec2, size: Vec2, low: Float, high: Float, value: Float, window: Window): OutlinedSlider(size, low, high, value, window) {
    override val pointer: SliderPointer = BasicSliderPointer(this, 1f)
}