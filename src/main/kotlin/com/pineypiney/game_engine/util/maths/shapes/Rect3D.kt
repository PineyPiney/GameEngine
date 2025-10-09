package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.f
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.PI

class Rect3D(val origin: Vec3, val side1: Vec3, val side2: Vec3) : Shape3D() {

	constructor(rect: Rect2D) : this(
		Vec3(rect.origin),
		Vec3(Vec2.fromAngle(rect.angle + (PI.f * 0.5f), rect.lengths.x)),
		Vec3(Vec2.fromAngle(rect.angle, rect.lengths.y))
	)

	val center: Vec3 get() = origin + (side1 + side2) * .5f
	override val min: Vec3
	override val max: Vec3

	init {
		val p = points
		min = p.reduce { a, b -> Vec3(minOf(a.x, b.x), minOf(a.y, b.y), minOf(a.z, b.z))}
		max = p.reduce { a, b -> Vec3(maxOf(a.x, b.x), maxOf(a.y, b.y), maxOf(a.z, b.z))}
	}

	val normal = (side1 cross side2).normalizeAssign()

	val points: Array<Vec3> get() = arrayOf(origin, origin + side1, origin + side2, origin + side1 + side2)

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersection = Plane(origin, normal).intersectedBy(ray).getOrNull(0) ?: return arrayOf()
		return if (containsPoint(intersection)) arrayOf(intersection)
		else arrayOf()
	}

	override infix fun containsPoint(point: Vec3): Boolean {

		// https://stackoverflow.com/a/8862483

		// P0P is the vector from the origin on the plane to the intersection
		val P0P = point - this.origin

		// That vector is then projected onto the sides of the rect, if those projections are shorter than both sides
		// then the point is inside the rect
		val q1 = P0P projectOn side1
		val q2 = P0P projectOn side2

		// For both projections check that the projection is a shorter version of the rectangle side
		val q1b =
			((q1.x != 0f && side1.x / q1.x >= 1) || side1.x == 0f) && ((q1.y != 0f && side1.y / q1.y >= 1) || side1.y == 0f) && ((q1.z != 0f && side1.z / q1.z >= 1) || side1.z == 0f)
		val q2b =
			((q2.x != 0f && side2.x / q2.x >= 1) || side2.x == 0f) && ((q2.y != 0f && side2.y / q2.y >= 1) || side2.y == 0f) && ((q2.z != 0f && side2.z / q2.z >= 1) || side2.z == 0f)

		return q1b && q2b
	}

	// https://gamedev.stackexchange.com/a/169389
	override fun vectorTo(point: Vec3): Vec3 {
		// op is point relative to the origin of the Rect
		val op = point - origin

		// a is how far along side1 op is
		val a = op dot side1
		// x is op projected onto side1 and then clamped to be within the rect
		val x: Vec3 = if (a < 0) Vec3(0f)
		else if (a > side1.length()) side1
		else op projectOn side1

		// b and y are a and x for side 2
		val b = op dot side2
		val y: Vec3 = if (b < 0) Vec3(0f)
		else if (b > side2.length()) side2
		else op projectOn side2

		// Using x and y we can get the closest point to point confined within the rect

		// Return the Difference
		val closestPoint = origin + x + y
		return point - closestPoint
	}

	override fun transformedBy(model: Mat4): Rect3D {
		val m = model.rotationComponent().scale(model.getScale())
		return Rect3D(origin + model.getTranslation(), side1.transformedBy(m), side2.transformedBy(m))
	}

	override fun getNormals(): Set<Vec3> {
		return setOf(side1.normalize(), side2.normalize(), normal)
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return points.map { it dot normal }.toSet()
	}
}