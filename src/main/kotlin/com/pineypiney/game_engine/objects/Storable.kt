package com.pineypiney.game_engine.objects

interface Storable {

    // Every Storable object has a list of all object collections it is stored in.
    // This makes it easier to delete objects and make sure they are not being stored in random places
    var objects: ObjectCollection?

    // These function define where in an object collection an object is stored
    fun addTo(objects: ObjectCollection)
    fun removeFrom(objects: ObjectCollection)
}