package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import glm_.vec2.Vec2

class BasicSlider(override var origin: Vec2, override val size: Vec2, override val low: Float, override val high: Float, override var value: Float, override val window: Window): OutlinedSlider() {
    override val pointer: SliderPointer = BasicSliderPointer(this, 1f)
}