package com.pineypiney.game_engine.visual

import glm_.mat4x4.Mat4

interface Renderable: Visual {

    fun render(vp: Mat4, tickDelta: Double)

    fun renderInstanced(amount: Int, vp: Mat4, tickDelta: Double) = render(vp, tickDelta)
}