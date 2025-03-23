package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.util.extension_functions.addToCollectionOr
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.filterIsInstance
import com.pineypiney.game_engine.util.extension_functions.forEachInstance

open class ObjectCollection {

	open val map = mutableMapOf<Int, ObjectCollectionLayer>()

	open val gameItems get() = get(0)
	open val guiItems get() = get(1)

	open fun addObject(o: GameObject?) {
		if (o != null) {
			// Add the object to this
			map.addToCollectionOr(o.layer, o) { ObjectCollectionLayer(o.layer) }

			// Add this to the object
			o.objects = this
		}
	}

	open fun addObjects(os: Iterable<GameObject>) {
		for (o in os) {
			val cur = map[o.layer]
			if (cur != null) cur.add(o)
			else map[o.layer] = ObjectCollectionLayer(o.layer, o)
			o.objects = this
		}
	}

	open fun removeObject(o: GameObject?) {
		if (o != null) {
			// Remove the object from this
			map[o.layer]?.remove(o)

			// Remove this from the object
			o.objects = null
		}
	}

	open fun removeObjects(os: Iterable<GameObject>) {
		for (o in os) {
			map[o.layer]?.remove(o)
			o.objects = this
		}
	}


	open fun update(interval: Float) {
		getAllComponents().forEachInstance<UpdatingComponent> {
			it.update(interval)
		}
	}

	open fun forAllObjects(layer: Int? = null, includeInactive: Boolean = false, action: GameObject.() -> Unit){
		fun processLayer(layer: MutableSet<GameObject>) { for(o in layer) if(includeInactive) o.forAllDescendants(action) else o.forAllActiveDescendants(action) }
		if(layer == null) for((_, layer) in map) processLayer(layer)
		else processLayer(get(layer))
	}

	open fun getAllObjects(layer: Int? = null, includeInactive: Boolean = false): Set<GameObject> {
		val func: GameObject.() -> Set<GameObject> =
			if (includeInactive) GameObject::allDescendants else GameObject::allActiveDescendants
		val heads = layer?.let { get(it) } ?: map.values.flatten()
		return heads.flatMap(func).toSet()
	}

	open fun getAllComponents(): Set<ComponentI> {
		return map.flatMap { (_, s) -> s.flatMap { o -> o.allActiveDescendants().flatMap { it.components } } }.toSet()
	}

	inline fun <reified T : ComponentI> getAllComponentInstances(layer: Int? = null, predicate: (T) -> Boolean = {true}): Set<T> {
		return if(layer != null) get(layer).flatMap { o -> o.allActiveDescendants().flatMap { it.components.filterIsInstance<ComponentI, T>(predicate) } }.toSet()
		else map.flatMap { (_, s) -> s.flatMap { o -> o.allActiveDescendants().flatMap { it.components.filterIsInstance<ComponentI, T>(predicate) } } }.toSet()
	}

	fun getAllInteractables(sort: Boolean = true): Set<InteractorComponent> {
		val components = getAllComponentInstances<InteractorComponent>()
		return (if (sort) components.sortedByDescending { it.passThrough } else components).toSet()
	}

	fun getAll2DCollisions(): Set<Collider2DComponent> {
		return getAllObjects().mapNotNull { it.getComponent<Collider2DComponent>() }.toSet()
	}

	fun getAll3DCollisions(): Set<Collider3DComponent> {
		return getAllObjects().mapNotNull { it.getComponent<Collider3DComponent>() }.toSet()
	}

	operator fun get(layer: Int) = map[layer] ?: mutableSetOf()
	operator fun set(layer: Int, collection: ObjectCollectionLayer) {
		map[layer] = collection
	}

	operator fun set(layer: Int, obj: GameObject) {
		map.addToCollectionOr(layer, obj) { ObjectCollectionLayer(obj.layer) }
	}

	fun delete() {
		getAllObjects().delete()
	}

	fun find(name: String): GameObject?{
		for(l in map){
			for(f in l.value){
				for(o in f.allDescendants()) if(o.name == name) return o
			}
		}
		return null
	}

	fun find(name: String, layer: Int): GameObject?{
		val l = map[layer] ?: return null
		for(f in l){
			for(o in f.allDescendants()) if(o.name == name) return o
		}
		return null
	}

	fun findTop(name: String, layer: Int): GameObject?{
		val l = map[layer] ?: return null
		for(o in l){
			if(o.name == name) return o
		}
		return null
	}

	inline fun <reified T : GameObject> get(name: String? = null): T? {
		return getAllObjects().filterIsInstance<T>().firstOrNull { it.name == name || name == null }
	}
}