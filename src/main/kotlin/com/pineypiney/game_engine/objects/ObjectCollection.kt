package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2D
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.forEachInstance

open class ObjectCollection {

    open val gameItems = mutableSetOf<GameObject>()
    open val guiItems = mutableSetOf<MenuItem>()

    open fun addObject(o: Storable?){
        // Add the object to this
        o?.addTo(this)

        // Add this to the object
        o?.objects?.add(this)
    }
    open fun removeObject(o: Storable?){
        // Remove the object from this
        o?.removeFrom(this)

        // Remove this from the object
        o?.objects?.remove(this)
    }


    open fun update(interval: Float){
        val time = Timer.time
        getAllObjects().forEachInstance<Updateable>{
            it.update(interval, time)
        }
    }

    open fun getAllObjects(): Set<Initialisable>{
        return guiItems + gameItems
    }

    fun getAllInteractables(sort: Boolean = true): Set<Interactable>{
        val items = getAllObjects().filterIsInstance<Interactable>()
        return (if(sort) items.sortedByDescending { it.importance } else items).toSet()
    }

    fun getAllCollisions(): Set<CollisionBox2D>{
        return getAllObjects().filterIsInstance<Collidable>().map { it.collider }.toSet()
    }

    fun delete(){
        getAllObjects().delete()
    }
}