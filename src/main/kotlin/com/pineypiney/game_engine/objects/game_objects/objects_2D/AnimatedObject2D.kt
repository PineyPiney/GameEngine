package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.Animatable
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.mat4x4.Mat4

abstract class AnimatedObject2D(shader: Shader): RenderedGameObject2D(shader), Animatable {

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)

        // For animated items the texture must be set to the animations current frame
        calcCurrentFrame().bind()
    }
}