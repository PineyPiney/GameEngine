package com.pineypiney.game_engine.visual

interface Storable {

    val objects: MutableList<ScreenObjectCollection>

    fun addTo(objects: ScreenObjectCollection)
    fun removeFrom(objects: ScreenObjectCollection)
}