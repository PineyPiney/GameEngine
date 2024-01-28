package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.f
import glm_.mat4x4.Mat4

abstract class AnimatedObject2D(shader: Shader): RenderedGameObject2D(shader), Animated {

    override var animationTime: Float = 0f
    override var playing: Boolean = true

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)

        // For animated items the texture must be set to the animations current frame
        if(playing) updateAnimationTime()
        val p = getProperties()
        for((key, value) in p) setProperty(key, value)
    }

    fun updateAnimationTime(){
        animationTime = (animationTime + Timer.frameDelta).f % animation.length
    }
}