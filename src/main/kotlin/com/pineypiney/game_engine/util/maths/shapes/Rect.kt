package com.pineypiney.game_engine.util.maths.shapes

import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Rect(origin: Vec3, val side1: Vec3, val side2: Vec3): Plane(origin, (side1 cross side2).normalizeAssign()) {

    constructor(origin: Vec2, size: Vec2): this(Vec3(origin, 0), Vec3(size.x, 0, 0), Vec3(0, size.y, 0))


}