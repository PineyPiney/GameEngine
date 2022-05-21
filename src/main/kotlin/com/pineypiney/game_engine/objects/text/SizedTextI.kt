package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window

interface SizedTextI: TextI {

    val fontSize: Int
    val separation: Float

    val lines: Array<String>
    val lengths: FloatArray

    fun generateLines(window: Window): Array<String>
}