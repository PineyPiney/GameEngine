package com.pineypiney.game_engine.util.raycasting

import glm_.vec3.Vec3

class Ray(val rayOrigin: Vec3, val direction: Vec3) {

    override fun toString(): String {
        return "Ray[$rayOrigin, $direction]"
    }
}