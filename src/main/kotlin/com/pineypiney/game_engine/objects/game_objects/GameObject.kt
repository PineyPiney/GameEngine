package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform
import glm_.mat4x4.Mat4

abstract class GameObject : Initialisable, Storable {

    override var objects: ObjectCollection? = null

    var name: String = "GameObject"
    abstract val transform: Transform<*, *, *>
    var parent: GameObject? = null

    val relativeModel: Mat4 get() = transform.model
    val worldModel: Mat4 get() = parent?.let { it.worldModel * relativeModel } ?: relativeModel

    override fun init() {}

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