package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform

abstract class GameObject : Initialisable, Storable {

    override val objects: MutableSet<ObjectCollection> = mutableSetOf()

    abstract val transform: Transform<*, *, *>

    override fun init() {}

    override fun addTo(objects: ObjectCollection){
        objects.gameItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.gameItems.remove(this)
    }

    override fun delete() {
        for(o in objects) { o.gameItems.remove(this) }
    }
}