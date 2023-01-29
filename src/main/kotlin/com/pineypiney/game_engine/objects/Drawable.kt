package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.util.maths.I
import glm_.vec2.Vec2
import glm_.vec3.Vec3

interface Drawable: Visual {

    val origin: Vec2
    val size: Vec2
    val model; get() = I.translate(Vec3(origin)).scale(Vec3(size))

    fun draw()

    fun updateAspectRatio(window: WindowI) {}
}