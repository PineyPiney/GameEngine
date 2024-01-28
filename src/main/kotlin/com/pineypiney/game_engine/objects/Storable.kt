package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.util.components.Component

interface Storable {

    var name: String

    // Every Storable object has a list of all object collections it is stored in.
    // This makes it easier to delete objects and make sure they are not being stored in random places
    var objects: ObjectCollection?
    val components: MutableSet<Component>
    val children: MutableSet<Storable>
    val parent: Storable? get() = objects?.getAllObjects()?.firstOrNull { it.children.contains(this) }

    fun setProperty(key: String, value: String){
        val keys = key.split('.')
        if(keys.size > 2) {
            val childName = keys[0]
            val child = objects?.getAllObjects()?.firstOrNull { it.name == childName }
            child?.setProperty(key.substringAfter('.'), value)
        }
        else if(keys.size == 2){
            components.firstOrNull { it.id == keys[0] }?.setValue(keys[1], value)
        }
    }

    // These function define where in an object collection an object is stored
    fun addTo(objects: ObjectCollection)
    fun removeFrom(objects: ObjectCollection)

    fun getChild(name: String) = children.firstOrNull { it.name == name }

    fun getComponent(id: String): Component?{
        val parts = id.split('.')
        if(parts.size == 1) return components.firstOrNull { it.id == parts[0] }
        return children.firstOrNull { it.name == parts[0] }?.getComponent(id.substring(parts[0].length + 1))
    }

    fun allDescendants(): Set<Storable>{
        val s = children.toMutableSet()
        for(c in children) s.addAll(c.allDescendants())
        return s
    }
}

inline fun <reified T: Component> Storable.getComponent(): T?{
    return components.firstOrNull { it is T } as? T
}