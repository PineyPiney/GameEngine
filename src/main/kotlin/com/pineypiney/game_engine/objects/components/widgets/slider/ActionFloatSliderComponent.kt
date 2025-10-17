package com.pineypiney.game_engine.objects.components.widgets.slider

import com.pineypiney.game_engine.objects.GameObject

open class ActionFloatSliderComponent(
	parent: GameObject,
	override val low: Float,
	override val high: Float,
	value: Float,
	action: (ActionSliderComponent<Float>) -> Unit
) : ActionSliderComponent<Float>(parent, value, action) {

	override val range: Float get() = high - low

	override fun getDelta(): Float = (value - low) / range
	override fun valueFromDelta(delta: Float): Float = (low + (delta * range)).coerceIn(low, high)
}