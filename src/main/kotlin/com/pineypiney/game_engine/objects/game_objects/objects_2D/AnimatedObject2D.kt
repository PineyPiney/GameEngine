package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.Animatable
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import glm_.mat4x4.Mat4

abstract class AnimatedObject2D(id: ResourceKey): SimpleTexturedGameObject2D(id, Texture.brokeTexture), Animatable {

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {

        // For animated items the texture must be set to the animations current frame
        texture = calcCurrentFrame()

        super.render(view, projection, tickDelta)
    }
}