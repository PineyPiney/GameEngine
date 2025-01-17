package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject

open class FloatSliderComponent(
	parent: GameObject,
	override val low: Float,
	override val high: Float,
	value: Float
) : SliderComponent<Float>(parent, value) {

	override val range: Float get() = high - low

	override fun getDelta(): Float = (value - low) / range
	override fun valueFromDelta(delta: Float): Float = (low + (delta * range)).coerceIn(low, high)
}