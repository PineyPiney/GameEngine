package com.pineypiney.game_engine.objects

import glm_.mat4x4.Mat4

interface Renderable: Visual, Shaded {

    fun render(view: Mat4, projection: Mat4, tickDelta: Double){
        shader.use()
        shader.setUniforms(uniforms)
        shader.setMat4("view", view)
        shader.setMat4("projection", projection)
    }
}