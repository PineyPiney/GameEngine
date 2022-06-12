package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.util.Transform
import glm_.mat4x4.Mat4

interface Renderable: Visual, Shaded {

    fun render(view: Mat4, projection: Mat4, tickDelta: Double){
        shader.use()
        shader.setUniforms(uniforms)
        shader.setMat4("view", view)
        shader.setMat4("projection", projection)
    }

    fun renderInstanced(transforms: Array<Transform>, view: Mat4, projection: Mat4, tickDelta: Double){
        shader.use()
        shader.setUniforms(uniforms)
        shader.setMat4("view", view)
        shader.setMat4("projection", projection)
        shader.setVec2s("translations", transforms.map { it.position }.toTypedArray())
        shader.setFloats("rotations", transforms.map { it.rotation }.toFloatArray())
        shader.setVec2s("scales", transforms.map { it.scale }.toTypedArray())
    }
}