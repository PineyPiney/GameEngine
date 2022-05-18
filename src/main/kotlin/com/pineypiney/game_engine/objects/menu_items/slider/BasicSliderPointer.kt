package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import glm_.vec2.Vec2

class BasicSliderPointer(override val parent: Slider, size: Vec2): SliderPointer(size * Vec2((size.y / size.x) * (pointerTexture.aspectRatio / Window.INSTANCE.aspectRatio), 1)) {
}