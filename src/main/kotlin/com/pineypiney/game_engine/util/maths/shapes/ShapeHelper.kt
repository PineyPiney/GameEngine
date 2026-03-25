package com.pineypiney.game_engine.util.maths.shapes

import glm_.mat2x2.Mat2
import glm_.vec2.Vec2
import kotlin.math.max
import kotlin.math.min

object ShapeHelper {

	fun solveCramers(mat: Mat2, vec: Vec2): Vec2 {
		mat.transpose(mat)
		println(mat)
		val det = mat.det
		val invDet = 1f / det
		val det0 = Mat2(mat).apply { set(0, vec); println(this) }.det
		val det1 = Mat2(mat).apply { set(1, vec); println(this) }.det
		return Vec2(det0 * invDet, det1 * invDet)
	}

	fun getLines(polygon: List<Int>): Array<Pair<Int, Int>> {
		return Array(polygon.size) { i ->
			val min = min(polygon[i], polygon[(i + 1) % polygon.size])
			val max = max(polygon[i], polygon[(i + 1) % polygon.size])
			min to max
		}
	}
}