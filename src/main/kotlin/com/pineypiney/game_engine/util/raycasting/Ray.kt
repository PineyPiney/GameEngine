package com.pineypiney.game_engine.util.raycasting

import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec3.Vec3

class Ray(val rayOrigin: Vec3, val direction: Vec3) {

	fun distanceTo(shape: Shape<*>): Float{
		return shape.intersectedBy(this).minOfOrNull { (it - rayOrigin).length() } ?: -1f
	}

	override fun toString(): String {
		return "Ray[$rayOrigin, $direction]"
	}
}