package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.game_objects.objects_2D.Animated
import com.pineypiney.game_engine.objects.menu_items.slider.BasicSliderPointer
import com.pineypiney.game_engine.objects.menu_items.slider.OutlinedSlider
import com.pineypiney.game_engine.objects.menu_items.slider.SliderPointer
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import kotlin.math.min

class AnimationTimeLine(val animatorLogic: AnimatorLogic, override val origin: Vec2, override val size: Vec2) : OutlinedSlider(){

    override val pointer: SliderPointer = BasicSliderPointer(this, 1f)
    override val low: Float = 0f
    override var high: Float = 1f
    override var value: Float = 0f
    override val window: WindowI = animatorLogic.window

    override fun moveSliderTo(move: Float) {
        super.moveSliderTo(move)
        val a = animatorLogic.o as? Animated ?: return
        a.animationTime = value
    }

    fun setAnimationLength(length: Float){
        high = length
        value = min(value, high)
    }
}