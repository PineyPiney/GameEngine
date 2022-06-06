package com.pineypiney.game_engine.objects.text

interface SizedTextI: TextI {

    val fontSize: Int
    val separation: Float

    val lines: Array<String>
    val lengths: FloatArray

    fun generateLines(): Array<String>
}