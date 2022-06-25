package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.util.Copyable

/**
 * Expand FloatArray using [entry] until its size is at least [size]
 * @param [size] The minimum size this list should be
 * @param [entry] The entry to add to the list
 *
 * @return A new list expanded to [size] using [entry]
 */
fun FloatArray.expand(size: Int, entry: Float = 0f): FloatArray{
    val a =  FloatArray(size)
    this.copyInto(a)
    for(i in this.size until size) a[i] = entry
    return a
}

/**
 * Expand IntArray using [entry] until its size is at least [size]
 * @param [size] The minimum size this list should be
 * @param [entry] The entry to add to the list
 *
 * @return A new list expanded to [size] using [entry]
 */
fun IntArray.expand(size: Int, entry: Int = 0): IntArray{
    val a =  IntArray(size)
    this.copyInto(a)
    for(i in this.size until size) a[i] = entry
    return a
}

/**
 * Copies every item in the array
 *
 * @return A new array of the copies of the original elements
 */
inline fun <reified E: Copyable<E>> Array<E>.copy() = map { i -> i.copy() }.toTypedArray()
