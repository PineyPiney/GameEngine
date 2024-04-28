package com.pineypiney.game_engine.objects.components

interface UpdatingComponent: ComponentI {

    fun update(interval: Float)
}