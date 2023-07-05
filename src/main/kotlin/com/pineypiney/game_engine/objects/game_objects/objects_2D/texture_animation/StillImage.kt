package com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation

import com.pineypiney.game_engine.resources.textures.Texture

class StillImage(val texture: Texture = Texture.broke): FrameSelector {
    override fun getTexture(time: Float): Texture = texture
}