package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.slider.SliderComponent
import kotlin.math.min

class AnimationTimeLine(parent: GameObject, val animatorLogic: AnimatorLogic) : SliderComponent(parent, 0f, 1f, 0f) {

	override var high: Float = 1f; protected set

	override fun moveSliderTo(move: Float) {
		super.moveSliderTo(move)
		val a = animatorLogic.o?.getComponent<AnimatedComponent>() ?: return
		a.animationTime = value
	}

	fun setAnimationLength(length: Float) {
		high = length
		value = min(value, high)
	}
}