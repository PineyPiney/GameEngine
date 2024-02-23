package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation.Animation
import glm_.f

class AnimatedComponent(parent: GameObject, var animation: Animation, val animations: List<Animation> = listOf(animation)): Component("ANI", parent), PreRenderComponent {

    constructor(parent: GameObject): this(parent, Animation.default)

    var animationTime: Float = 0f
    var playing: Boolean = true

    override val fields: Array<Field<*>> = arrayOf(
        Field("ani", ::DefaultFieldEditor, ::animation, {animation = it}, Animation::name, { c, s -> animations.firstOrNull { it.name == s }}),
        FloatField("tim", ::animationTime){ animationTime = it },
        BooleanField("ply", ::playing){ playing = it }
    )

    override fun preRender(tickDelta: Double) {
        // For animated items the texture must be set to the animations current frame
        if(playing) updateAnimationTime()
        val p = getProperties()
        for((key, value) in p) parent.setProperty(key, value)
    }

    fun setAnimation(name: String){
        animations.firstOrNull { it.name == name }?.let { animation = it }
    }

    fun getProperties(): Map<String, String> {
        return animation.getProperties(animationTime)
    }

    fun updateAnimationTime(){
        animationTime = (animationTime + Timer.frameDelta).f % animation.length
    }
}