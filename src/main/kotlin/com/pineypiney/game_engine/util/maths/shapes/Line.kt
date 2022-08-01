package com.pineypiney.game_engine.util.maths.shapes

import glm_.vec3.Vec3
import kotlin.math.abs

class Line(val start: Vec3, val end: Vec3) {
    val grad = (end - start).normalize()

    // https://stackoverflow.com/a/8862483
    infix fun intersects(plane: Plane): Vec3?{

        val dot = plane.normal dot grad

        // If the direction is perpendicular to the normal then they do not cross over
        if(abs(dot) < 1e-6) return null

        // a is the distance along the ray to the intersection with the plane of the rectangle
        val a = ((plane.point - start) dot plane.normal) / dot

        // return the point on the plane
        return start + grad * a
    }
}