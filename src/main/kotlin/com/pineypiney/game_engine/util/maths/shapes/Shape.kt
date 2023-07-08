package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec3.Vec3

abstract class Shape {

    abstract fun intersectedBy(ray: Ray): Array<Vec3>
    abstract fun containsPoint(point: Vec3): Boolean
}