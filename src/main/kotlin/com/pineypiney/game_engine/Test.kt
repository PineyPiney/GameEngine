package com.pineypiney.game_engine

import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

fun main(){
    val copy = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor)

    val rect1 = Rect2D(Vec2(36.5, -3.2), 1.26f, 2.88f, 0f)
    val rect2 = Rect2D(Vec2(37.5, -5), 1f, 10f, 0f)
    val o = rect1.overlap1D(Vec2(1, 0), rect2)
    println("Done with o $o")
}