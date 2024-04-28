package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.util.extension_functions.addToCollectionOr
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.forEachInstance

open class ObjectCollection {

    open val map = mutableMapOf<Int, MutableSet<GameObject>>()


    open val gameItems get() = get(0)
    open val guiItems get() = get(1)

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

    open fun getAllObjects(includeInactive: Boolean = false): Set<GameObject>{
        val func: GameObject.() -> Set<GameObject> = if(includeInactive) GameObject::allDescendants else GameObject::allActiveDescendants
        return map.values.flatten().flatMap(func).toSet()
    }

    open fun getAllComponents(): Set<Component>{
        return map.flatMap { (_, s) -> s.flatMap{ o -> o.allActiveDescendants().flatMap { it.components } } }.toSet()
    }

    inline fun <reified T: Component> getAllComponentInstances(): Set<T>{
        return getAllObjects().mapNotNull { it.getComponent<T>() }.toSet()
    }

    fun getAllInteractables(sort: Boolean = true): Set<InteractorComponent>{
        val components = getAllComponents().filterIsInstance<InteractorComponent>()
        return (if(sort) components.sortedByDescending { it.importance } else components).toSet()
    }

    fun getAllCollisions(): Set<ColliderComponent>{
        return getAllObjects().mapNotNull { it.getComponent<ColliderComponent>() }.toSet()
    }

    operator fun get(layer: Int) = map[layer] ?: mutableSetOf()
    operator fun set(layer: Int, set: MutableSet<GameObject>){ map[layer] = set }
    operator fun set(layer: Int, obj: GameObject){
        map.addToCollectionOr(layer, obj){ mutableSetOf() }
    }

    fun delete(){
        getAllObjects().delete()
    }

    inline fun <reified T: GameObject> get(name: String? = null): T?{
        return getAllObjects().filterIsInstance<T>().firstOrNull { it.name == name || name == null }
    }
}