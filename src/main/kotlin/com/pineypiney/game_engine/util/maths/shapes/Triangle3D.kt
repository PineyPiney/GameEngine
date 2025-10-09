package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.Vectors
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class Triangle3D(val point1: Vec3, val point2: Vec3, val point3: Vec3) : Shape3D() {

	val side1 = point2 - point1
	val side2 = point3 - point1

	val normal = (side1 cross side2).normalizeAssign()
	val points: Array<Vec3> get() = arrayOf(point1, point2, point3)

	override val min: Vec3
	override val max: Vec3

	init {
		val minMax = Vectors.minMaxVec3(listOf(point1, point2, point3))
		min = minMax.first
		max = minMax.second
	}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersection = Plane(point1, normal).intersectedBy(ray).firstOrNull() ?: return arrayOf()
		return if (containsPoint(intersection)) arrayOf(intersection)
		else arrayOf()
	}

	override infix fun containsPoint(point: Vec3): Boolean {

		// https://stackoverflow.com/a/8862483

		// P0P is the vector from the origin on the plane to the intersection
		val ap = point - this.point1

		// That vector is then projected onto the sides of the triangle,
		// if the distance across both lengths is between 0 and 1
		// and the sum does not exceed 1 then it is within the triangle

		val q1 = ap projectionMult side1
		val q2 = ap projectionMult side2

		return q1 in 0f..1f && q2 in 0f..1f && q1 + q2 <= 1f
	}

	// TODO("Test")
	// https://stackoverflow.com/a/74395029
	override fun vectorTo(point: Vec3): Vec3 {
		val vector2Plane = (point1 - point) projectOn normal
		val pop = point + vector2Plane

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
		if (vc <= 0f && d1 >= 0f && d3 <= 0f)
		{
			val  v = d1 / (d1 - d3)
			return point - (point1 + side1 * v)
		}

		val vb = d5 * d2 - d1 * d6
		if (vb <= 0f && d2 >= 0f && d6 <= 0f)
		{
			val  v = d2 / (d2 - d6)
			return point - (point1 + side2 * v)
		}

		val va = d3 * d6 - d5 * d4
		if (va <= 0f && (d4 - d3) >= 0f && (d5 - d6) >= 0f)
		{
			val  v = (d4 - d3) / ((d4 - d3) + (d5 - d6))
			return point - (point2 + (point3 - point2) * v)
		}

		val denom = 1f / (va + vb + vc)
		val v = vb * denom
		val w = vc * denom
		return point - (point1 + (side1 * v) + (side2 * w))
	}

	override fun transformedBy(model: Mat4): Triangle3D {
		val m = model.rotationComponent().scale(model.getScale())
		val p1 = point1 + model.getTranslation()
		return Triangle3D(p1, p1 + side1.transformedBy(m), p1 + side2.transformedBy(m))
	}

	override fun getNormals(): Set<Vec3> {
		return setOf((side1 cross normal).normalize(), (side2 cross normal).normalize(), ((point3 - point2) cross normal).normalize(), normal)
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return points.map { it dot normal }.toSet()
	}
}