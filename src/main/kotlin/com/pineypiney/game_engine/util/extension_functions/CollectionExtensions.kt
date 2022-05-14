package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.IScreenObject

fun <E: Deleteable> Collection<E?>?.delete(){
    this?.forEach {
        it?.delete()
    }
}

fun <E: Deleteable> Map<*, E?>.delete(){
    forEach {
        it.value?.delete()
    }
}

fun <E, T> MutableMap<E, T>.getOrSet(key: E, create: (key: E) -> T): T{
    val current = getOrNull(key)
    return if(current != null) current
    else{
        val new = create.invoke(key)
        this[key] = new
        new
    }
}

fun <K, V: MutableCollection<E>, E> MutableMap<K, V>.addToListOr(key: K, value: E, create: (key: K) -> V){
    this.putIfAbsent(key, create(key))
    this[key]?.add(value)
}

fun <K, V: MutableCollection<E>, E> MutableMap<K, V>.combineLists(other: MutableMap<K, V>): MutableMap<K, V>{
    val newMap = this.toMutableMap()
    for((key, value) in other){
        if(this.containsKey(key)) newMap[key]?.addAll(value)
        else newMap[key] = value
    }
    return newMap
}

fun <E: IScreenObject> Collection<E?>?.init(){
    this?.forEach {
        it?.init()
    }

}

fun <E: IScreenObject> Map<*, E?>.init(){
    forEach {
        it.value?.init()
    }
}

fun <K, V> Map<K, V>.getOrNull(key: K): V? = getOrDefault(key, null)

inline fun <reified K> Map<*, *>.withKeys(): Map<K, Any>{
    return asType<K, Any>()
}

inline fun <reified K, reified V> Map<*, *>.asType(): Map<K, V>{
    val map: MutableMap<K, V> = mutableMapOf()
    forEach{ (key, value) ->
        if(key is K && value is V){
            map[key] = value
        }
    }
    return map.toMap()
}