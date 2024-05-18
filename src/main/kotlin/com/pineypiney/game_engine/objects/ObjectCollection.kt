package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.*
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

    open fun getAllObjects(layer: Int? = null, includeInactive: Boolean = false): Set<GameObject>{
        val func: GameObject.() -> Set<GameObject> = if(includeInactive) GameObject::allDescendants else GameObject::allActiveDescendants
        val heads = layer?.let { get(it) } ?: map.values.flatten()
        return heads.flatMap(func).toSet()
    }

    open fun getAllComponents(): Set<Component>{
        return map.flatMap { (_, s) -> s.flatMap{ o -> o.allActiveDescendants().flatMap { it.components } } }.toSet()
    }

    inline fun <reified T: Component> getAllComponentInstances(layer: Int? = null): Set<T>{
        return getAllObjects(layer).mapNotNull { it.getComponent<T>() }.toSet()
    }

    fun getAllInteractables(sort: Boolean = true): Set<InteractorComponent>{
        val components = getAllComponents().filterIsInstance<InteractorComponent>()
        return (if(sort) components.sortedByDescending { it.importance } else components).toSet()
    }

    fun getAll2DCollisions(): Set<Collider2DComponent>{
        return getAllObjects().mapNotNull { it.getComponent<Collider2DComponent>() }.toSet()
    }

    fun getAll3DCollisions(): Set<Collider3DComponent>{
        return getAllObjects().mapNotNull { it.getComponent<Collider3DComponent>() }.toSet()
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