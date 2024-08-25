package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.f
import glm_.vec3.Vec3

class StringParser {

	fun parseVec3(string: String): Vec3 {
		val values: List<String> = string
			.removePrefix("{")
			.removeSuffix("}")
			.replaceWhiteSpaces()
			.split(",")


		val vec = Vec3()

		for (it in values) {
			val axis: Char = it[0]
			val value: Float = it.filter { char -> numberDigits.contains(char) }.f

			when (axis) {
				'x' -> vec.x = value
				'y' -> vec.y = value
				'z' -> vec.z = value
			}
		}

		return vec
	}

	companion object {
		val numberDigits: Array<Char> = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.')
	}
}