package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.rendering.RendererI

interface Renderable: Visual, Shaded {

    fun render(renderer: RendererI<*>, tickDelta: Double){
        shader.setUp(uniforms, renderer)
        shader.setVP(renderer.view, renderer.projection)
    }
}