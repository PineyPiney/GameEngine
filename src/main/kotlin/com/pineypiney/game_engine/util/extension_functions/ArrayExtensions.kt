package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.util.Copyable

fun FloatArray.expand(size: Int, entry: Float = 0f): List<Float>{
    val list = this.toMutableList()
    while(list.size < size) list.add(entry)
    return list
}

fun IntArray.expand(size: Int, entry: Int = 0): List<Int>{
    val list = this.toMutableList()
    while(list.size < size) list.add(entry)
    return list
}

inline fun <reified E: Copyable<E>> Array<E>.copy() = map { i -> i.copy() }.toTypedArray()
