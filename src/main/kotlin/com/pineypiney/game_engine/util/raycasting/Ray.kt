package com.pineypiney.game_engine.util.raycasting

import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.maths.shapes.Line
import com.pineypiney.game_engine.util.maths.shapes.Rect
import glm_.vec3.Vec3

class Ray(val rayOrigin: Vec3, val direction: Vec3) {

    // https://stackoverflow.com/a/8862483
    fun passesThroughRect(rect: Rect): Boolean{

        // p is the point in the plane that the ray passes through
        val p = Line(rayOrigin, rayOrigin + direction).intersects(rect) ?: return false

        // P0P is the vector from the origin on the plane to the intersection
        val P0P = p - rect.point

        // That vector is then projected onto the sides of the rect, if those projections are shorter than both sides
        // then the point is inside the rect
        val q1 = P0P projectOn rect.side1
        val q2 = P0P projectOn rect.side2

        // TODO("find a better way of checking the projections are in the same direction as the sides (not backwards), and are shorter than the sides")
        return ((0 <= q1.x && q1.x <= rect.side1.x) || (rect.side1.x <= q1.x && q1.x <= 0)) && ((0 <= q1.y && q1.y <= rect.side1.y) || (rect.side1.y <= q1.y && q1.y <= 0)) &&
                ((0 <= q2.x && q2.x <= rect.side2.x) || (rect.side2.x <= q2.x && q2.x <= 0)) && ((0 <= q2.y && q2.y <= rect.side2.y) || (rect.side2.y <= q2.y && q2.y <= 0))
    }
}