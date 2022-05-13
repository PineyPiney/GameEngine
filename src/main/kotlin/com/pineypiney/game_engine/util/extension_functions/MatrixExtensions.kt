package com.pineypiney.game_engine.util.extension_functions

import glm_.f
import glm_.mat4x4.Mat4


fun Mat4.Companion.create(list: Iterable<Number>, index: Int = 0): Mat4 {
    return Mat4(
        list.elementAt(index).f, list.elementAt(index + 4).f, list.elementAt(index + 8).f, list.elementAt(index + 12).f,
        list.elementAt(index + 1).f, list.elementAt(index + 5).f, list.elementAt(index + 9).f, list.elementAt(index + 13).f,
        list.elementAt(index + 2).f, list.elementAt(index + 6).f, list.elementAt(index + 10).f, list.elementAt(index + 14).f,
        list.elementAt(index + 3).f, list.elementAt(index + 7).f, list.elementAt(index + 11).f, list.elementAt(index + 15).f
    )
}

fun Mat4.printM(name: String){
    println("$name Matrix is: ")
    for(i in 0..3){
        println("[${this[i][0].round(2)}, " +
                "\t${this[i][1].round(2)}, " +
                "\t${this[i][2].round(2)}, " +
                "\t${this[i][3].round(2)}]")
    }
}

val Mat4.c get() = Mat4(
    this[0][0], this[0][1], this[0][2], this[0][3],
    this[1][0], this[1][1], this[1][2], this[1][3],
    this[2][0], this[2][1], this[2][2], this[2][3],
    this[3][0], this[3][1], this[3][2], this[3][3]
)
