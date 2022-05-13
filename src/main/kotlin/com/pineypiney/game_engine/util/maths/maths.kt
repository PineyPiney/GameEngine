package com.pineypiney.game_engine.util

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.*

val I = Mat4(1)
val O = Vec3(0)
val up = Vec3(0.0, 1.0, 0.0)
val normal = Vec3(0, 0, -1)

val sin30 = sin(PI/6)
val sin60 = sin(PI/3)

val tan30 = tan(PI/6)
val tan60 = tan(PI/3)
