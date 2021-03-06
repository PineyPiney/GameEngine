package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.Initialisable

/**
 * Initialise all items in a collection of initialisable objects
 */
fun <E: Initialisable> Collection<E?>?.init(){
    this?.forEach {
        it?.init()
    }

}

/**
 * Initialise all items in a map of initialisable objects
 */
fun <E: Initialisable> Map<*, E?>.init(){
    for(i in values) i?.init()
}

/**
 * Delete all items in a collection of deletable objects
 */
fun <E: Deleteable> Collection<E?>?.delete(){
    this?.forEach {
        it?.delete()
    }
}

/**
 * Delete all items in a map of deletable objects
 */
fun <E: Deleteable> Map<*, E?>.delete(){
    for(i in values) i?.delete()
}

/**
 * Expand a list of floats using [entry] until its size is at least [size]
 * @param [size] The minimum size this list should be
 * @param [entry] The entry to add to the list
 *
 * @return A new list expanded to [size] using [entry]
 */
fun List<Float>.expand(size: Int, entry: Float = 0f): List<Float>{
    val list = this.toMutableList()
    while(list.size < size) list.add(entry)
    return list
}

/**
 * Expand a list of ints using [entry] until its size is at least [size]
 * @param [size] The minimum size this list should be
 * @param [entry] The entry to add to the list
 *
 * @return A new list expanded to [size] using [entry]
 */
fun List<Int>.expand(size: Int, entry: Int = 0): List<Int>{
    val list = this.toMutableList()
    while(list.size < size) list.add(entry)
    return list
}

/**
 * Returns a list containing all elements that are not instances of specified type parameter R.
 *
 * @return A new list with no elements of type R
 */
inline fun <reified R, reified E> Collection<E>.filterIsNotInstance(): Collection<E>{
    return this.filter { it !is R }
}

/**
 * Performs [action] on all elements that are instances of specified type parameter R
 *
 * @param [action] The function called on each instance of R
 */
inline fun <reified R> Collection<*>.forEachInstance(action: (R) -> Unit){
    filterIsInstance<R>().forEach(action)
}

/**
 * Remove and return the first element in the list that satisfies [predicate]
 *
 * @param [predicate] The requirements for the element to be removed
 *
 * @return The removed element
 */
fun <E> MutableCollection<E>.popFirst(predicate: (E) -> Boolean): E{
    val pop = first(predicate)
    remove(pop)
    return pop
}

/**
 * Remove and return the first element in the list that satisfies [predicate], or null if no elements satisfy
 *
 * @param [predicate] The requirements for the element to be removed
 *
 * @return The removed element, or null
 */
fun <E> MutableCollection<E>.popFirstOrNull(predicate: (E) -> Boolean): E?{
    val pop = firstOrNull(predicate)
    remove(pop)
    return pop
}


/**
 * Get the element at [key], or if it doesn't exist then set the element at [key] using [create]
 *
 * @param [key] The key to get the value at
 * @param [create] The constructor to create a value at if there is not one set yet
 *
 * @return The value at [key]
 */
fun <E, T> MutableMap<E, T>.getOrSet(key: E, create: (key: E) -> T): T{
    val current = getOrNull(key)
    return if(current != null) current
    else{
        val new = create(key)
        this[key] = new
        new
    }
}

/**
 * Add [value] to the mutable collection at [key], or create a new mutable collection with [value] using [create]
 *
 * @param [key] The key of the map to add [value] to
 * @param [value] The value to add to the collection at [key]
 * @param [create] The constructor to create a new mutable collection with
 */
fun <K, V: MutableCollection<E>, E> MutableMap<K, V>.addToListOr(key: K, value: E, create: (key: K) -> V){
    this.putIfAbsent(key, create(key))
    this[key]?.add(value)
}

/**
 * Add the collections of two maps using the same keys and values, when the values are a type of collection
 *
 * @param [other] The collection to add to [this]
 *
 * @return A new map that has added the collections of [this] and [other] together by key
 */
fun <K, V: MutableCollection<E>, E> MutableMap<K, V>.combineLists(other: MutableMap<K, V>): MutableMap<K, V>{
    val newMap = this.toMutableMap()
    for((key, value) in other){
        if(this.containsKey(key)) newMap[key]?.addAll(value)
        else newMap[key] = value
    }
    return newMap
}

/**
 * Try to get the values at [key], or return null
 *
 * @param key The key to get the value at
 *
 * @return The values at key, or null if there is none
 */
fun <K, V> Map<K, V>.getOrNull(key: K): V? = getOrDefault(key, null)

/**
 * Filter the map for all the key-value pairs that fits the specified types K and V
 *
 * @return A map of keys K and values V
 */
inline fun <reified K, reified V> Map<*, *>.asType(): Map<K, V>{
    val map: MutableMap<K, V> = mutableMapOf()
    for((key, value) in this){
        if(key is K && value is V){
            map[key] = value
        }
    }
    return map.toMap()
}

/**
 * Remove all the pairs with null keys from the map
 *
 * @return a new map with no null keys
 */
inline fun <reified K, reified V> Map<K?, V>.removeNullKeys(): Map<K, V>{
    return this.entries.filter { it.key != null }.associate { it.key!! to it.value }
}

/**
 * Remove all the pairs with null values from the map
 *
 * @return a new map with no null values
 */
inline fun <reified K, reified V> Map<K, V?>.removeNullValues(): Map<K, V>{
    return this.entries.filter { it.value != null }.associate { it.key to it.value!! }
}