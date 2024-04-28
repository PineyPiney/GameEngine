package com.pineypiney.game_engine.objects.components

interface PreRenderComponent: ComponentI {
    val whenVisible: Boolean
    fun preRender(tickDelta: Double)
}