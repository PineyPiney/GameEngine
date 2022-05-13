package com.pineypiney.game_engine.visual

interface Updateable {

    fun update(interval: Float, time: Double)
    fun shouldUpdate(): Boolean

}