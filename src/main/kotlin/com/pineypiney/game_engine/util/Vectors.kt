package com.pineypiney.game_engine.util

import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Vectors {

	companion object {

		fun minMaxVec2(values: Iterable<Vec2>): Pair<Vec2, Vec2> {

			val min = Vec2(values.first())
			val max = Vec2(values.first())
			for ((x, y) in values) {
				min.x = minOf(min.x, x)
				min.y = minOf(min.y, y)
				max.x = maxOf(max.x, x)
				max.y = maxOf(max.y, y)
			}
			return min to max
		}

		fun minMaxVec3(values: Iterable<Vec3>): Pair<Vec3, Vec3> {

			val min = Vec3(values.first())
			val max = Vec3(values.first())
			for ((x, y, z) in values) {
				min.x = minOf(min.x, x)
				min.y = minOf(min.y, y)
				min.z = minOf(min.z, z)
				max.x = maxOf(max.x, x)
				max.y = maxOf(max.y, y)
				max.z = maxOf(max.z, z)
			}
			return min to max
		}
	}
}