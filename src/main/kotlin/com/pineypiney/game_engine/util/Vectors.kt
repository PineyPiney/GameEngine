package com.pineypiney.game_engine.util

import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Vectors {

	companion object {

		fun minMaxVec2(values: List<Vec2>): Pair<Vec2, Vec2> {

			val min = Vec2(values.first())
			val max = Vec2(values.first())
			for (i in 1..<values.size) {
				min.x = minOf(min.x, values[i].x)
				min.y = minOf(min.y, values[i].y)
				max.x = maxOf(max.x, values[i].x)
				max.y = maxOf(max.y, values[i].y)
			}
			return min to max
		}
		fun minMaxVec3(values: List<Vec3>): Pair<Vec3, Vec3> {

			val min = Vec3(values.first())
			val max = Vec3(values.first())
			for (i in 1..<values.size) {
				min.x = minOf(min.x, values[i].x)
				min.y = minOf(min.y, values[i].y)
				min.z = minOf(min.z, values[i].z)
				max.x = maxOf(max.x, values[i].x)
				max.y = maxOf(max.y, values[i].y)
				max.z = maxOf(max.z, values[i].z)
			}
			return min to max
		}
	}
}