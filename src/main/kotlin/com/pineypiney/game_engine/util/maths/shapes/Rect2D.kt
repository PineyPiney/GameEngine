package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.extension_functions.reduceA
import glm_.f
import glm_.func.common.abs
import glm_.vec2.Vec2
import kotlin.math.*

class Rect2D(val origin: Vec2, val length1: Float, val length2: Float, val angle: Float = 0f){

    constructor(origin: Vec2, size: Vec2, angle: Float = 0f): this(origin, size.x, size.y, angle)
    val size: Vec2 get() = Vec2(length1, length2)
    val side1: Vec2 get() = Vec2(length1 * cos(angle), length1 * -sin(angle))
    val side2: Vec2 get() = Vec2(length2 * sin(angle), length2 * cos(angle))

    val normal1: Vec2 get() = side1.normal().normalize()
    val normal2: Vec2 get() = side2.normal().normalize()

    val points: Array<Vec2> get() = arrayOf(origin, origin + side1, origin + side2, origin + side1 + side2)

    fun overlap1D(normal: Vec2, other: Rect2D): Float {

        val range1 = projectTo(normal)
        val range2 = other projectTo normal

        return if (range1.x > range2.y || range2.x > range1.y) 0f
                else floatArrayOf(range2.y - range1.x, range1.y - range2.x).minBy { it.abs } *
                if(normal.x.abs > 0) normal.x.sign else normal.y.sign
    }

    infix fun overlapVector(other: Rect2D): Vec2{
        val lengths = normals(other).associateWith { overlap1D(it, other) }
        return lengths.minBy { it.value.abs }.run { key * value }
    }

    // https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
    infix fun intersects(other: Rect2D): Boolean{
        return !normals(other).any { overlap1D(it, other) == 0f }
    }

    infix fun projectTo(normal: Vec2): Vec2{
        return points.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, vec3 ->
            val pp = vec3.projectOn(normal)
            val p = pp.length() * if (pp.angle() >= PI) -1 else 1
            Vec2(min(acc.x, p), max(acc.y, p))
        }
    }

    infix fun normals(other: Rect2D) =
            if(angle.mod(PI.f / 2) == other.angle.mod(PI.f / 2)) arrayOf(normal1, normal2)
            else arrayOf(normal1, normal2, other.normal1, other.normal2)

    override fun toString(): String {
        return "Rect2D[$origin]"
    }
}