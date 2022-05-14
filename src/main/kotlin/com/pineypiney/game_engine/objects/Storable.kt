package com.pineypiney.game_engine.objects

interface Storable {

    val objects: MutableList<ScreenObjectCollection>

    fun addTo(objects: ScreenObjectCollection)
    fun removeFrom(objects: ScreenObjectCollection)
}