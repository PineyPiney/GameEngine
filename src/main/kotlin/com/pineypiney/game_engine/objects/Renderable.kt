package com.pineypiney.game_engine.objects

import glm_.mat4x4.Mat4

interface Renderable: Visual, Shaded {

    fun render(view: Mat4, projection: Mat4, tickDelta: Double){
        shader.setUp(uniforms)
        shader.setVP(view, projection)
    }
}