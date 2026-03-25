package com.pineypiney.game_engine.util

class CollectionMap<K, V, C : MutableCollection<V>> : LinkedHashMap<K, C> {

	val factory: (K) -> C

	constructor(factory: (K) -> C) : super() {
		this.factory = factory
	}

	constructor(factory: () -> C) : super() {
		this.factory = { factory() }
	}

	constructor(factory: (K) -> C, initialCapacity: Int, loadFactor: Float = .75f) : super(initialCapacity, loadFactor) {
		this.factory = factory
	}

	fun copy(): CollectionMap<K, V, C> {
		val map = CollectionMap(factory)
		for ((k, v) in this) {
			map[k] = v
		}
		return map
	}

	/**
	 * Add [value] to the mutable collection at [key], or create a new mutable collection with [value] using [factory]
	 *
	 * @param [key] The key of the map to add [value] to
	 * @param [value] The value to add to the collection at [key]
	 */
	fun add(key: K, value: V) {
		this.putIfAbsent(key, factory(key))
		this[key]?.add(value)
	}

	/**
	 * Add the collections of two maps using the same keys and values, when the values are a type of collection
	 *
	 * The collections in this are shallow copied
	 *
	 * @param [other] The collection to add to this
	 *
	 * @return A new map that has added the collections of this and [other] together by key
	 */
	fun combine(other: CollectionMap<out K, out V, out C>): CollectionMap<K, V, C> {
		val newMap = copy()
		for ((key, value) in other) {
			if (newMap.containsKey(key)) newMap[key]!!.addAll(value)
			else newMap[key] = value
		}
		return newMap
	}

	companion object {
		fun <K, V> list() = CollectionMap<K, V, MutableList<V>>(::ArrayList)
		fun <K, V> set() = CollectionMap<K, V, MutableSet<V>>(::LinkedHashSet)
	}
}