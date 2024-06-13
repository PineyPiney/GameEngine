package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.f
import glm_.func.common.abs
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.*

class Rect2D(var origin: Vec2, var length1: Float, var length2: Float, var angle: Float = 0f): Shape(){

    constructor(origin: Vec2, size: Vec2, angle: Float = 0f): this(origin, size.x, size.y, angle)
    var size: Vec2
        get() = Vec2(length1, length2)
        set(value) { length1 = value.x; length2 = value.y }
    val side1: Vec2 get() = Vec2(length1 * cos(angle), length1 * -sin(angle))
    val side2: Vec2 get() = Vec2(length2 * sin(angle), length2 * cos(angle))

    val normal1: Vec2 get() = side1.normal().normalize()
    val normal2: Vec2 get() = side2.normal().normalize()

    val points: Array<Vec2> get() = arrayOf(origin, origin + side1, origin + side2, origin + side1 + side2)

    fun overlap1D(normal: Vec2, other: Rect2D): Float {

        // The range of the normal that each rect takes up
        val range1 = projectTo(normal)
        val range2 = other projectTo normal

        // If the two ranges don't overlap then return 0
        return if (range1.x > range2.y || range2.x > range1.y) 0f
                // Otherwise get the smaller of the two overlaps between the ranges
                else floatArrayOf(range2.x - range1.y, range2.y - range1.x).minBy { it.abs }
    }

    infix fun overlapVector(other: Rect2D): Vec2{
        val lengths = normals(other).associateWith { overlap1D(it, other) }
        val r = lengths.minBy { it.value.abs }.run { key * value }
        return r
    }

    // https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
    infix fun intersects(other: Rect2D): Boolean{
        return normals(other).all { overlap1D(it, other) != 0f }
    }

    infix fun projectTo(normal: Vec2): Vec2{
        return points.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, vec2 ->
            val pp = vec2.projectOn(normal)
            val p = pp.length() * if (pp.angle() != normal.angle()) -1 else 1
            Vec2(min(acc.x, p), max(acc.y, p))
        }
    }

    fun normals(other: Rect2D? = null) =
            if(other == null || angle.mod(PI.f / 2) == other.angle.mod(PI.f / 2)) arrayOf(normal1, normal2)
            else arrayOf(normal1, normal2, other.normal1, other.normal2)

    override fun intersectedBy(ray: Ray): Array<Vec3> {
        val intersection = Plane(Vec3(origin), Vec3(0f, 0f, 1f)).intersectedBy(ray).getOrNull(0) ?: return arrayOf()
        return if(containsPoint(intersection)) arrayOf(intersection)
        else arrayOf()
    }

    override fun containsPoint(point: Vec3): Boolean {
        return Vec2(point).isWithin(origin, size)
    }

    override fun vectorTo(point: Vec3): Vec3 {
        // Lazy
        return Rect3D(this) vectorTo point
    }

    override fun transformedBy(model: Mat4): Rect2D {
        val scale = Vec2(model.getScale())
        val rotation = model.getRotation().eulerAngles().z
        return Rect2D((origin.rotate(rotation) * scale) + Vec2(model.getTranslation()), size * scale, angle - rotation)
    }

    override fun toString(): String {
        return "Rect2D[$origin, $size]"
    }
}