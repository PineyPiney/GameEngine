package com.pineypiney.game_engine.visual.game_objects

import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.visual.Animatable
import glm_.mat4x4.Mat4

abstract class AnimatedObject(id: ResourceKey): TexturedGameObject(id, Texture.brokeTexture), Animatable {

    override fun render(vp: Mat4, tickDelta: Double) {

        // For animated items the texture must be set to the animations current frame
        texture = calcCurrentFrame()

        super.render(vp, tickDelta)
    }
}