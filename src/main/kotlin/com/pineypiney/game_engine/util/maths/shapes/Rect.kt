package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.projectOn
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin

class Rect(origin: Vec3, val side1: Vec3, val side2: Vec3): Plane(origin, (side1 cross side2).normalizeAssign()) {

    constructor(origin: Vec2, size: Vec2): this(Vec3(origin, 0), Vec3(size.x, 0, 0), Vec3(0, size.y, 0))
    constructor(origin: Vec2, size: Vec2, angle: Float): this(Vec3(origin, 0), Vec3(size.x * cos(angle), size.y * sin(angle), 0), Vec3(size.x * sin(angle), size.y * cos(angle), 0))

    infix fun containsPoint(point: Vec3): Boolean{

        // P0P is the vector from the origin on the plane to the intersection
        val P0P = point - this.point

        // That vector is then projected onto the sides of the rect, if those projections are shorter than both sides
        // then the point is inside the rect
        val q1 = P0P projectOn side1
        val q2 = P0P projectOn side2

        // For both projections check that the projection is a shorter version of the rectangle side
        val q1b = ((q1.x != 0f && side1.x / q1.x >= 1) || side1.x == 0f) && ((q1.y != 0f && side1.y / q1.y >= 1) || side1.y == 0f)
        val q2b = ((q2.x != 0f && side2.x / q2.x >= 1) || side2.x == 0f) && ((q2.y != 0f && side2.y / q2.y >= 1) || side2.y == 0f)

        return q1b && q2b
    }
}