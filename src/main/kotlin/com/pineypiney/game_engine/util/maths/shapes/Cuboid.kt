package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.transformedBy
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.operators.div

class Cuboid(val center: Vec3, val rotation: Quat, val size: Vec3): Shape() {

    val min = center - (size / 2)
    val max = min + size

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

        return if(lastEnter < firstExit) arrayOf(rotatedOrigin + (rotatedDir * lastEnter), rotatedOrigin + (rotatedDir * firstExit))
        else arrayOf()
    }

    override fun containsPoint(point: Vec3): Boolean {

        if(min.x > point.x || point.x > max.x) return false
        if(min.y > point.y || point.y > max.y) return false
        return (min.z < point.z && point.z < max.z)
    }

    override fun transformedBy(model: Mat4): Shape {
        return Cuboid(center + model.getTranslation(), rotation * model.getRotation(), size * model.getScale())
    }
}