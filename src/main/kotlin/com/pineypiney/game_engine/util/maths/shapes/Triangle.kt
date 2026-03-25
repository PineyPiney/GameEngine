package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.Vectors
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.angleBetween
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.extension_functions.transformedBy
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat2x2.Mat2
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.abs

class Triangle(val point1: Vec2, val point2: Vec2, val point3: Vec2) : Shape2D() {

	val side1 = point2 - point1
	val side2 = point3 - point1
	val side3 = point3 - point2

	val points: Array<Vec2> get() = arrayOf(point1, point2, point3)

	override val min: Vec2
	override val max: Vec2
	val obtuseAngle: Int

	init {
		val minMax = Vectors.minMaxVec2(listOf(point1, point2, point3))
		min = minMax.first
		max = minMax.second

		obtuseAngle = if (abs(side1.angleBetween(side2)) > PIF * .5f) 0
		else if (abs(side1.angleBetween(side3)) < PIF * .5f) 1
		else if (abs(side2.angleBetween(side3)) > PIF * .5f) 2
		else -1
	}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersection = Plane(Vec3(point1), NORMAL).intersectedBy(ray).firstOrNull() ?: return arrayOf()
		return if (containsPoint(Vec2(intersection))) arrayOf(intersection)
		else arrayOf()
	}

	override infix fun containsPoint(point: Vec2): Boolean {

		// https://stackoverflow.com/a/2049593

		val bl1 = Line2D(point1, point2).onRightSide(point)
		return Line2D(point2, point3).onRightSide(point) == bl1
				&& Line2D(point3, point1).onRightSide(point) == bl1
	}

	// TODO("Test")
	// https://stackoverflow.com/a/74395029
	override fun vectorTo(point: Vec2): Vec2 {
		val pop = point - point1

		val ap = pop - point1
		val d1 = side1 dot ap
		val d2 = side2 dot ap
		if (d1 <= 0f && d2 <= 0f) return point - point1

		val bp = pop - point2
		val d3 = side1 dot bp
		val d4 = side2 dot bp
		if (d3 >= 0f && d4 <= d3) return point - point2


		val cp = pop - point3
		val d5 = side1 dot cp
		val d6 = side2 dot cp
		if (d6 >= 0f && d5 <= d6) return point - point3

		val vc = d1 * d4 - d3 * d2
		if (vc <= 0f && d1 >= 0f && d3 <= 0f) {
			val v = d1 / (d1 - d3)
			return point - (point1 + side1 * v)
		}

		val vb = d5 * d2 - d1 * d6
		if (vb <= 0f && d2 >= 0f && d6 <= 0f) {
			val v = d2 / (d2 - d6)
			return point - (point1 + side2 * v)
		}

		val va = d3 * d6 - d5 * d4
		if (va <= 0f && (d4 - d3) >= 0f && (d5 - d6) >= 0f) {
			val v = (d4 - d3) / ((d4 - d3) + (d5 - d6))
			return point - (point2 + (point3 - point2) * v)
		}

		val denom = 1f / (va + vb + vc)
		val v = vb * denom
		val w = vc * denom
		return point - (point1 + (side1 * v) + (side2 * w))
	}

	override fun getBoundingCircle(): Circle {
		return when (obtuseAngle) {
			0 -> {
				val center = (point2 + point3) * .5f
				Circle(center, (point3 - center).length())
			}

			1 -> {
				val center = (point1 + point3) * .5f
				Circle(center, (point1 - center).length())
			}

			2 -> {
				val center = (point2 + point1) * .5f
				Circle(center, (point2 - center).length())
			}

			else -> {
				val bisector1Point = (point1 + point2) * .5f
				val bisector1C = bisector1Point dot side1

				val bisector2Point = (point1 + point3) * .5f
				val bisector2C = bisector2Point dot side2

				val center = ShapeHelper.solveCramers(Mat2(side1, side2), Vec2(bisector1C, bisector2C))
				Circle(center, (point1 - center).length())
			}
		}
	}

	override fun transformedBy(model: Mat4): Triangle {
		val p1 = point1.transformedBy(model)
		val p2 = point2.transformedBy(model)
		val p3 = point3.transformedBy(model)
		return Triangle(p1, p2, p3)
	}

	override fun getNormals(): Set<Vec2> {
		return setOf(side1.normal(), side2.normal(), side3.normal())
	}

	override fun projectToNormal(normal: Vec2): Set<Float> {
		return points.map { it dot normal }.toSet()
	}
}