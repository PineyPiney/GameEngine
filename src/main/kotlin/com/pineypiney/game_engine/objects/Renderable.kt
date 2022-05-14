package com.pineypiney.game_engine.objects

import glm_.mat4x4.Mat4

interface Renderable: Visual {

    fun render(view: Mat4, projection: Mat4, tickDelta: Double)

    fun renderInstanced(amount: Int, view: Mat4, projection: Mat4, tickDelta: Double) = render(view, projection, tickDelta)
}