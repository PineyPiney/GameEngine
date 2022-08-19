package com.pineypiney.game_engine.util.raycasting

import com.pineypiney.game_engine.util.maths.shapes.Line
import com.pineypiney.game_engine.util.maths.shapes.Plane
import com.pineypiney.game_engine.util.maths.shapes.Rect
import glm_.vec3.Vec3

class Ray(val rayOrigin: Vec3, val direction: Vec3) {

    infix fun intersects(plane: Plane) = Line(rayOrigin, rayOrigin + direction) intersects plane

    // https://stackoverflow.com/a/8862483
    fun passesThroughRect(rect: Rect): Boolean{

        // p is the point in the plane that the ray passes through
        val p = intersects(rect) ?: return false
        return rect containsPoint p
    }
}