package com.pineypiney.game_engine

import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2

fun main(){
    val r1 = Rect2D(Vec2(0), 1f, 1f)
    val r2 = Rect2D(Vec2(-1.4, 0), 1f, 1f, 0.78f)
    val i = r1 overlapVector r2
    println("Intersects: $i")
}