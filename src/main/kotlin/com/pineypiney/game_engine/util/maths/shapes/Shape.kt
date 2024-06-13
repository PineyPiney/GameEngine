package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

abstract class Shape {

    abstract infix fun intersectedBy(ray: Ray): Array<Vec3>
    abstract infix fun containsPoint(point: Vec3): Boolean
    abstract infix fun vectorTo(point: Vec3): Vec3

    abstract infix fun transformedBy(model: Mat4): Shape
}