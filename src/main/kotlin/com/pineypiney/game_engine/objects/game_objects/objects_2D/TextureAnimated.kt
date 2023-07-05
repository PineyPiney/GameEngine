package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation.FrameSelector
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.f

interface TextureAnimated {

    val frameSelector: FrameSelector
    val animationOffset: Float

    fun chooseTexture(): Texture {
        val progress = (Timer.frameTime.f - animationOffset)
        return frameSelector.getTexture(progress)
    }
}