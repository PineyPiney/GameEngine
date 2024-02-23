package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class Plane(val point: Vec3, val normal: Vec3): Shape() {
    override fun intersectedBy(ray: Ray): Array<Vec3> {

        // https://stackoverflow.com/a/8862483
        val dot = normal dot ray.direction

        // If the direction is perpendicular to the normal then they do not cross over
        if(abs(dot) < 1e-6) return arrayOf()

        // a is the distance along the ray to the intersection with the plane of the rectangle
        val a = ((point - ray.rayOrigin) dot normal) / dot

        // return the point on the plane
        return arrayOf(ray.rayOrigin + (ray.direction * a))
    }

    override fun containsPoint(point: Vec3): Boolean {
        val (m, c) = getEquation()
        return abs((m dot point) + c) < 1e-6
    }

    override fun transformedBy(model: Mat4): Shape {
        return Plane(point + model.getTranslation(), Vec3(model.rotationComponent() * Vec4(normal, 1f)))
    }

    fun getEquation(): Pair<Vec3, Float>{
        val inv = -normal
        val d = -(inv dot point)
        return inv to d
    }
}