package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.operators.div

class AxisAlignedCuboid(val center: Vec3, val size: Vec3): Shape() {

    val min = center - (size / 2)
    val max = min + size

    override fun intersectedBy(ray: Ray): Array<Vec3> {
        val invMag = 1f / ray.direction

        // The distances of the ray to the boundaries of the box,
        // scaled to the direction of the ray, so it can be considered
        // the number of steps the ray must take to reach the boundary
        val stepsToMin = (min - ray.rayOrigin) * invMag
        val stepsToMax = (max - ray.rayOrigin) * invMag

        val (minX, maxX) = if(stepsToMin.x < stepsToMax.x) Vec2(stepsToMin.x, stepsToMax.x) else Vec2(stepsToMax.x, stepsToMin.x)
        val (minY, maxY) = if(stepsToMin.y < stepsToMax.y) Vec2(stepsToMin.y, stepsToMax.y) else Vec2(stepsToMax.y, stepsToMin.y)
        val (minZ, maxZ) = if(stepsToMin.z < stepsToMax.z) Vec2(stepsToMin.z, stepsToMax.z) else Vec2(stepsToMax.z, stepsToMin.z)

        val lastEnter = maxOf(minX, minY, minZ)
        val firstExit = minOf(maxX, maxY, maxZ)

        return if(lastEnter < firstExit) arrayOf(ray.rayOrigin + (ray.direction * lastEnter), ray.rayOrigin + (ray.direction * firstExit))
        else arrayOf()
    }

    override fun containsPoint(point: Vec3): Boolean {

        if(min.x > point.x || point.x > max.x) return false
        if(min.y > point.y || point.y > max.y) return false
        return (min.z < point.z && point.z < max.z)
    }
}