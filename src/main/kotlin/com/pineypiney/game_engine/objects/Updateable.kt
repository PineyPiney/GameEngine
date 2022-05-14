package com.pineypiney.game_engine.objects

interface Updateable {

    fun update(interval: Float, time: Double)
    fun shouldUpdate(): Boolean

}