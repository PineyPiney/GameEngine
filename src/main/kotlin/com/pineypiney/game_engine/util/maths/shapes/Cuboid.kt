package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.func.common.abs
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.operators.div
import kotlin.math.abs

class Cuboid(var center: Vec3, var rotation: Quat, var size: Vec3): Shape() {

    val side1 get() = Vec3(size.x, 0f, 0f).rotate(rotation)
    val side2 get() = Vec3(0f, size.y, 0f).rotate(rotation)
    val side3 get() = Vec3(0f, 0f, size.z).rotate(rotation)

    val points: Array<Vec3> get() {
        val sides = arrayOf(side1, side2, side3)
        val o = center - (size.rotate(rotation) * .5f)
        return Array(8){
            val v = Vec3(o)
            for(i in 0..2) if(it and (1 shl i) > 0) v += sides[i]
            v
        }
    }

    override fun intersectedBy(ray: Ray): Array<Vec3> {

        val rotationMatrix = rotation.toMat4().inverse()
        val rotatedOrigin = ray.rayOrigin transformedBy rotationMatrix
        val rotatedDir = ray.direction transformedBy rotationMatrix

        val rotatedCenter = center transformedBy rotationMatrix
        val rotatedMin = rotatedCenter - (size / 2)
        val rotatedMax = rotatedMin + size

        val invMag = 1f / rotatedDir
        // The distances of the ray to the boundaries of the box,
        // scaled to the direction of the ray, so it can be considered
        // the number of steps the ray must take to reach the boundary
        val stepsToMin = (rotatedMin - rotatedOrigin) * invMag
        val stepsToMax = (rotatedMax - rotatedOrigin) * invMag

        val (minX, maxX) = if(stepsToMin.x < stepsToMax.x) Vec2(stepsToMin.x, stepsToMax.x) else Vec2(stepsToMax.x, stepsToMin.x)
        val (minY, maxY) = if(stepsToMin.y < stepsToMax.y) Vec2(stepsToMin.y, stepsToMax.y) else Vec2(stepsToMax.y, stepsToMin.y)
        val (minZ, maxZ) = if(stepsToMin.z < stepsToMax.z) Vec2(stepsToMin.z, stepsToMax.z) else Vec2(stepsToMax.z, stepsToMin.z)

        val lastEnter = maxOf(minX, minY, minZ)
        val firstExit = minOf(maxX, maxY, maxZ)

        return if(0f < lastEnter && lastEnter < firstExit) arrayOf(rotatedOrigin + (rotatedDir * lastEnter), rotatedOrigin + (rotatedDir * firstExit))
        else arrayOf()
    }

    override fun containsPoint(point: Vec3): Boolean {
        val vec = point - center

        val project1 = vec projectOn side1.normalize()
        val project2 = vec projectOn side2.normalize()
        val project3 = vec projectOn side3.normalize()

        return project1.length() <= abs(size.x) * .5f &&
                project2.length() <= abs(size.y) * .5f &&
                project3.length() <= abs(size.z) * .5f
    }

    override fun transformedBy(model: Mat4): Cuboid {
        val scale = model.getScale()
        val rotation = model.getRotation()
        //return Rect2D((origin.rotate(rotation) * scale) + Vec2(model.getTranslation()), size * scale, angle - rotation)
        return Cuboid(center.rotate(rotation) * scale + model.getTranslation(), this.rotation * rotation, size * scale)
    }

    fun overlap1D(normal: Vec3, other: Cuboid): Vec2 {

        // The range of the normal that each rect takes up
        val range1 = projectTo(normal)
        val range2 = other projectTo normal

        // If the two ranges don't overlap then return 0
        if (range1.x >= range2.y || range2.x >= range1.y) return Vec2(0f)
        // Otherwise pass the distances needed to separate the cuboids in each direction,
        // where the first value is positive and the second is negative
        val a = range2.x - range1.y
        val b = range2.y - range1.x
        return if(a > 0f) Vec2(a, b) else Vec2(b, a)
    }

    infix fun overlapVector(other: Cuboid): Vec3{
        val lengths = normals(other).associateWith { overlap1D(it, other).x }
        val r = lengths.minBy { it.value.abs }.run { key * value }
        return r
    }

    fun getEjection(other: Cuboid, movement: Vec3): Vec3{
        val still = movement == Vec3(0f)
        val moveMag = movement.length()
        val lengths = normals(other).associateWith {
            val overlaps = overlap1D(it, other)
            // If there is no movement then just pick the smallest movement
            if (still) {
                absMinOf(overlaps.x, overlaps.y)
            }
            // Otherwise pick the one to move back against the original movement
            else{
                val dot = it dot movement
                // If the movement is in the other direction to the normal then use the positive magnitude (x)
                // otherwise use the negative version
                overlaps.run { if (dot < 0f) x else y }
            }
        }

        // If any of the normals don't overlap then the cuboids also don't overlap so no escape vector is needed
        if(lengths.any { it.value == 0f }) return Vec3(0f)

        // If there is movement then check for a vector parallel to the movement, which would be used automatically
        if(!still) {
            for ((normal, mag) in lengths) {
                val dot = normal dot movement
                if (abs(dot) > moveMag * .9f && abs(mag) <= moveMag) return normal * mag
            }
        }
        val r = lengths.minBy { it.value.abs }.run { key * value }
        return r
    }

    infix fun intersects(other: Cuboid): Boolean {
        return normals(other).all { overlap1D(it, other).x != 0f }
    }

    infix fun projectTo(normal: Vec3): Vec2{
        return points.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, vec3 ->
            val pp = vec3.projectOn(normal)
            // Normalise pp twice because of floating point errors
            val p = pp.length() * if (pp.normalize().normalize() != normal.normalize()) -1 else 1
            Vec2(kotlin.math.min(acc.x, p), kotlin.math.max(acc.y, p))
        }
    }

    fun normals(other: Cuboid? = null) =
        (if(other == null || rotation == other.rotation) arrayOf(side1, side2, side3)
        else arrayOf(side1, side2, side3, other.side1, other.side2, other.side3)).map { it.normalize() }

}