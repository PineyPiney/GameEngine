package com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation

import com.pineypiney.game_engine.objects.game_objects.objects_2D.AnimatedObject2D
import com.pineypiney.game_engine.resources.textures.Texture


/**
 * [FrameSelector] is an interface extended by any class that is used to select a texture
 * based on a float input. [AnimatedObject2D] uses a frame selector to hold it's [TextureAnimation]
 * so that it can be swapped out for a [StillImage] if so desired.
 */
interface FrameSelector {

    fun getTexture(time: Float): Texture
}