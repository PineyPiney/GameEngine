package com.pineypiney.game_engine.util.maths

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.*

val I = Mat4(1f)
val O = Vec3(0f)
val up = Vec3(0f, 1f, 0f)
val normal = Vec3(0f, 0f, -1f)

val sin30 = sin(PI/6)
val sin60 = sin(PI/3)

val tan30 = tan(PI/6)
val tan60 = tan(PI/3)

fun eulerToVector(yaw: Double, pitch: Double, res: Vec3 = Vec3()): Vec3{
    res.x = (cos(yaw) * cos(pitch)).toFloat()
    res.y = sin(pitch).toFloat()
    res.z = (sin(yaw) * cos(pitch)).toFloat()
    res.normalize()
    return res
}

fun vectorToEuler(v: Vec3): Pair<Float, Float>{
    val p = asin(v.y)
    val y = asin(v.z / cos(p))
    return p to y
}