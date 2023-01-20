package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.objects.Animatable
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.mat4x4.Mat4

abstract class AnimatedObject3D(): SimpleTexturedGameObject3D(Texture.broke), Animatable {

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {

        // For animated items the texture must be set to the animations current frame
        texture = calcCurrentFrame()

        super.render(view, projection, tickDelta)
    }
}