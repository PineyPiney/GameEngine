package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.util.extension_functions.delete

open class ObjectCollectionLayer(val layer: Int, initialObjects: Set<GameObject>) : LinkedHashSet<GameObject>(initialObjects) {

	@Suppress("UNCHECKED_CAST")
	constructor(layer: Int, vararg initialObjects: GameObject): this(layer, (initialObjects as Array<GameObject>).toSet())

	open fun getAllObjects(includeInactive: Boolean = false): Set<GameObject> {
		val func: GameObject.() -> Set<GameObject> =
			if (includeInactive) GameObject::allDescendants else GameObject::allActiveDescendants
		return flatMap(func).toSet()
	}

	open fun getAllComponents(): Set<ComponentI> {
		return flatMap { o -> o.allActiveDescendants().flatMap { it.components } }.toSet()
	}

	inline fun <reified T : ComponentI> getAllComponentInstances(): Set<T> {
		return mapNotNull { it.getComponent<T>() }.toSet()
	}

	fun getAllInteractables(sort: Boolean = true): Set<InteractorComponent> {
		val components = getAllComponents().filterIsInstance<InteractorComponent>()
		return (if (sort) components.sortedByDescending { it.passThrough } else components).toSet()
	}

	fun getAll2DCollisions(): Set<Collider2DComponent> {
		return getAllObjects().mapNotNull { it.getComponent<Collider2DComponent>() }.toSet()
	}

	fun getAll3DCollisions(): Set<Collider3DComponent> {
		return getAllObjects().mapNotNull { it.getComponent<Collider3DComponent>() }.toSet()
	}

	operator fun get(name: String) = find(name)

	fun delete() {
		getAllObjects().delete()
	}

	fun find(name: String): GameObject?{
		for(f in this){
			for(o in f.allDescendants()) if(o.name == name) return o
		}
		return null
	}

	fun findTop(name: String): GameObject?{
		for(o in this){
			if(o.name == name) return o
		}
		return null
	}
}