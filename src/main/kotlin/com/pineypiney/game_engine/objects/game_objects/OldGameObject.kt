package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection

open class OldGameObject : GameObject() {

    override fun addTo(objects: ObjectCollection){
        objects.gameItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.gameItems.remove(this)
    }

    override fun delete() {
        objects?.gameItems?.remove(this)
    }
}