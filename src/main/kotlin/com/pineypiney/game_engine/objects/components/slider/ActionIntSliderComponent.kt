package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import kotlin.math.roundToInt

open class ActionIntSliderComponent(
	parent: GameObject,
	override val low: Int,
	override val high: Int,
	value: Int,
	action: (ActionSliderComponent<Int>) -> Unit
) : ActionSliderComponent<Int>(parent, value, action) {

	override val range: Int get() = high - low

	override fun getDelta(): Float = (value.toFloat() - low) / range
	override fun valueFromDelta(delta: Float): Int = (low + (delta * range)).roundToInt().coerceIn(low, high)
}