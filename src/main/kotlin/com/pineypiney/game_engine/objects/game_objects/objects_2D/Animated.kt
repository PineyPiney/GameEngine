package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation.Animation

interface Animated {

    val animations: List<Animation>
    var animation: Animation

    var animationTime: Float
    var playing: Boolean

    fun setAnimation(name: String){
        animations.firstOrNull { it.name == name }?.let { animation = it }
    }

    fun getProperties(): Map<String, String> {
        return animation.getProperties(animationTime)
    }
}