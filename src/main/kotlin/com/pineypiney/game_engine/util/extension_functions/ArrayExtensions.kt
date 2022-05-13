package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.util.Copyable

fun DoubleArray.printV(){
    val s = this.size
    val v = s/8
    for(j in 0 until v){
        val i = j*8
        println("[${this[i]}, ${this[i+1]}, ${this[i+2]}], \t[${this[i+3]}, ${this[i+4]}, ${this[i+5]}], \t[${this[i+6]}, ${this[i+7]}]")
    }
}

fun FloatArray.printV(){
    val s = this.size
    val v = s/8
    for(j in 0 until v){
        val i = j*8
        println("[${this[i]}, ${this[i+1]}, ${this[i+2]}], \t[${this[i+3]}, ${this[i+4]}, ${this[i+5]}], \t[${this[i+6]}, ${this[i+7]}]")
    }
}

fun IntArray.printI(){
    val s = this.size
    val v = s/3
    for(j in 0 until v){
        val i = j*3
        println("[${this[i]}, ${this[i+1]}, ${this[i+2]}]")
    }
}

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
