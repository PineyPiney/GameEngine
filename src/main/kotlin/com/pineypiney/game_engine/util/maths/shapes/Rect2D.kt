package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.extension_functions.reduceA
import glm_.vec2.Vec2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Rect2D(val origin: Vec2, val length1: Float, val length2: Float, val angle: Float = 0f) {

    val side1: Vec2 get() = Vec2(length1 * cos(angle), length1 * sin(angle))
    val side2: Vec2 get() = Vec2(length2 * sin(angle), length2 * cos(angle))

    val normal1: Vec2 get() = side1.normal()
    val normal2: Vec2 get() = side2.normal()

    val points: Array<Vec2> get() = arrayOf(origin, origin + side1, origin + side2, origin + side1 + side2)

    fun overlap1D(normal: Vec2, shape1: Array<Vec2>, shape2: Array<Vec2>): Boolean{

        val range1 = shape1.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, vec3 ->
            val p = vec3.projectOn(normal).x
            Vec2(min(acc.x, p), max(acc.y, p))
        }
        val range2 = shape2.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, vec3 ->
            val p = vec3.projectOn(normal).x
            Vec2(min(acc.x, p), max(acc.y, p))
        }

        return range1.x < range2.y && range1.y > range2.x
    }

    // https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
    infix fun intersects(other: Rect2D): Boolean{

        return overlap1D((normal1), points, other.points) ||
                overlap1D((normal2), points, other.points) ||
                overlap1D((other.normal1), points, other.points) ||
                overlap1D((other.normal2), points, other.points)

    }

    override fun toString(): String {
        return "Rect2D[$origin]"
    }
}