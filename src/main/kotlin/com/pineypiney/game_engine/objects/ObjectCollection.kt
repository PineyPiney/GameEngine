package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.game_objects.OldGameObject
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.forEachInstance

open class ObjectCollection {

    open val gameItems = mutableSetOf<OldGameObject>()
    open val guiItems = mutableSetOf<MenuItem>()

    open fun addObject(o: GameObject?){
        // Add the object to this
        o?.addTo(this)

        // Add this to the object
        o?.objects = this
    }
    open fun removeObject(o: GameObject?){
        // Remove the object from this
        o?.removeFrom(this)

        // Remove this from the object
        o?.objects = this
    }


    open fun update(interval: Float){
        getAllComponents().forEachInstance<UpdatingComponent>{
            it.update(interval)
        }
    }

    open fun getAllObjects(): Set<GameObject>{
        return (guiItems + gameItems).flatMap { it.allDescendants() }.toSet()
    }

    open fun getAllComponents(): Set<Component>{
        return (guiItems + gameItems).flatMap { o -> o.allDescendants().flatMap { it.components } }.toSet()
    }

    fun getAllInteractables(sort: Boolean = true): Set<InteractorComponent>{
        val components = getAllComponents().filterIsInstance<InteractorComponent>()
        return (if(sort) components.sortedByDescending { it.importance } else components).toSet()
    }

    fun getAllCollisions(): Set<ColliderComponent>{
        return getAllObjects().mapNotNull { it.getComponent<ColliderComponent>() }.toSet()
    }

    fun delete(){
        getAllObjects().delete()
    }

    inline fun <reified T: GameObject> get(name: String? = null): T?{
        return getAllObjects().filterIsInstance<T>().firstOrNull { it.name == name || name == null }
    }
}