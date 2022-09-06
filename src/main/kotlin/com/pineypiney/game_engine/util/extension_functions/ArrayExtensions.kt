package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.util.Copyable

/**
 * Returns an array of Booleans containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An BooleanArray with all the results of [init]
 */
fun <E> Collection<E>.toBooleanArray(init: (E) -> Boolean): BooleanArray{
    val res = BooleanArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Bytes containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An ByteArray with all the results of [init]
 */
fun <E> Collection<E>.toByteArray(init: (E) -> Byte): ByteArray{
    val res = ByteArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Chars containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An CharArray with all the results of [init]
 */
fun <E> Collection<E>.toCharArray(init: (E) -> Char): CharArray{
    val res = CharArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Doubles containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An DoubleArray with all the results of [init]
 */
fun <E> Collection<E>.toDoubleArray(init: (E) -> Double): DoubleArray{
    val res = DoubleArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Floats containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An FloatArray with all the results of [init]
 */
fun <E> Collection<E>.toFloatArray(init: (E) -> Float): FloatArray{
    val res = FloatArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Ints containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An IntArray with all the results of [init]
 */
fun <E> Collection<E>.toIntArray(init: (E) -> Int): IntArray{
    val res = IntArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Longs containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An LongArray with all the results of [init]
 */
fun <E> Collection<E>.toLongArray(init: (E) -> Long): LongArray{
    val res = LongArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Returns an array of Shorts containing the result of [init] performed on all the elements of this collection.
 *
 * @param init the function to be performed on each element in this collection
 *
 * @return An ShortArray with all the results of [init]
 */
fun <E> Collection<E>.toShortArray(init: (E) -> Short): ShortArray{
    val res = ShortArray(size)
    var index = 0
    for(i in this){
        res[index++] = init(i)
    }
    return res
}

/**
 * Initialise all items in an array of initialisable objects
 */
fun <E: Initialisable> Array<E>.init(){
    forEach(Initialisable::init)
}

/**
 * Deletes all items in an array of deleteable objects
 */
fun <E: Deleteable> Array<E>.delete(){
    forEach(Deleteable::delete)
}

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

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 *
 * @param element The element to be removed
 */
operator fun <T> Array<T>.minus(element: T): Array<T>{
    val index = indexOf(element)
    if(index == -1) return this

    // sliceArray must be used so that the array isn't a nullable one
    val newArray = sliceArray(0 .. size - 2)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun ByteArray.minus(element: Byte): ByteArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun ShortArray.minus(element: Short): ShortArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun IntArray.minus(element: Int): IntArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun LongArray.minus(element: Long): LongArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun FloatArray.minus(element: Float): FloatArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun DoubleArray.minus(element: Double): DoubleArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun BooleanArray.minus(element: Boolean): BooleanArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an array containing all elements of the original array except the first instance that equals the given [element].
 */
operator fun CharArray.minus(element: Char): CharArray{
    val index = indexOfFirst{ it == element }
    if(index == -1) return this

    val newArray = copyOf(size - 1)
    for(i in (index..size - 2)) newArray[i] = this[i + 1]
    return newArray
}

/**
 * Returns an Array repeated [times] times.
 */
infix fun <T> Array<T>.repeat(times: Int): Array<T>{
    var a = copyOf()
    for(i in 2 .. times) {
        a += this
    }
    return a
}

/**
 * Returns a ByteArray repeated [times] times.
 */
infix fun ByteArray.repeat(times: Int): ByteArray{
    var a = copyOf()
    for(i in 2 .. times){
        a += this
    }
    return a
}
