package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.sqrt

class Parallelogram(val origin: Vec2, val side1: Vec2, val side2: Vec2) : Shape2D() {

	override val center: Vec2 = origin + (side1 + side2) * .5f
	val points = setOf(origin, origin + side1, origin + side1 + side2, origin + side2)
	override val min = points.reduce { a, b -> Vec2(minOf(a.x, b.x), minOf(a.y, b.y))}
	override val max = points.reduce { a, b -> Vec2(maxOf(a.x, b.x), maxOf(a.y, b.y))}

	override fun transformedBy(model: Mat4): Shape2D {
		val scale = model.getScale()
		val scale2D = Vec2(scale)
		val rotation = glm.roll(model.getRotation(scale))

		return Parallelogram(origin + model.getTranslation2D(), (side1 * scale2D).rotate(rotation), (side2 * scale2D).rotate(rotation))
	}

	override fun getBoundingCircle(): Circle {
		val diameter = sqrt(maxOf((side1 + side2).length2(), (side1 - side2).length2()))
		return Circle(center, diameter * .5f)
	}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersection = Plane(Vec3(origin), Vec3(0f, 0f, 1f)).intersectedBy(ray).getOrNull(0) ?: return arrayOf()
		return if (containsPoint(Vec2(intersection))) arrayOf(intersection)
		else arrayOf()
	}

	override fun containsPoint(point: Vec2): Boolean {
		// P0P is the vector from the origin on the plane to the intersection
		val P0P = point - this.origin

		val normal1 = side2 projectOn side1.normal().normalize()
		val normal2 = side1 projectOn side2.normal().normalize()

		// That vector is then projected onto the sides of the rect, if those projections are shorter than both sides
		// then the point is inside the rect
		val q1 = P0P projectOn normal1
		val q2 = P0P projectOn normal2

		// For both projections check that the projection is a shorter version of the rectangle side
		val q1b =
			((q1.x != 0f && normal1.x / q1.x >= 1) || normal1.x == 0f) &&
			((q1.y != 0f && normal1.y / q1.y >= 1) || normal1.y == 0f)
		val q2b =
			((q2.x != 0f && normal2.x / q2.x >= 1) || normal2.x == 0f) &&
			((q2.y != 0f && normal2.y / q2.y >= 1) || normal2.y == 0f)

		return q1b && q2b
	}

	override fun vectorTo(point: Vec2): Vec2 {
		val op = point - origin

		val side1 = side1
		val side2 = side2

		val a = op dot side1
		val x: Vec2 = if (a < 0) Vec2(0f)
		else if (a > side1.length()) side1
		else op projectOn side1

		val b = op dot side2
		val y: Vec2 = if (b < 0) Vec2(0f)
		else if (b > side2.length()) side2
		else op projectOn side2

		val closestPoint = origin + x + y
		return point - closestPoint
	}

	override fun getNormals(): Set<Vec2> {
		return setOf(side1.normal().normalize(), side2.normal().normalize())
	}

	override fun projectToNormal(normal: Vec2): Set<Float> {
		return projectAllPoints(normal, points)
	}

	override fun translate(move: Vec2) {
		origin += move
	}
}