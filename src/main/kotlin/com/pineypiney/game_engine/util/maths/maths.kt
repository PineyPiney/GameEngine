package com.pineypiney.game_engine.util.maths

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.tan

val I = Mat4(1f)
val O = Vec3(0f)
val up = Vec3(0f, 1f, 0f)
val normal = Vec3(0f, 0f, -1f)

val sin30 = sin(PI/6)
val sin60 = sin(PI/3)

val tan30 = tan(PI/6)
val tan60 = tan(PI/3)
