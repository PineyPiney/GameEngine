package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Rect2D(val origin: Vec2, val length1: Float, val length2: Float, val angle: Float = 0f) : Shape2D() {

	constructor(originX: Float, originY: Float, length1: Float, length2: Float, angle: Float = 0f): this(Vec2(originX, originY), length1, length2, angle)
	constructor(origin: Vec2, size: Vec2, angle: Float = 0f) : this(origin, size.x, size.y, angle)

	val lengths: Vec2 get() = Vec2(length1, length2)

	val side1: Vec2 get() = Vec2(length1 * cos(angle), length1 * -sin(angle))
	val side2: Vec2 get() = Vec2(length2 * sin(angle), length2 * cos(angle))

	val normal1: Vec2 get() = side2.normalize()
	val normal2: Vec2 get() = side1.normalize()

	val points: Set<Vec2> get() = setOf(origin, origin + side1, origin + side2, origin + side1 + side2)

	val center: Vec2 get() = origin + (side1 + side2) * .5f

	override val min: Vec2
	override val max: Vec2

	init {
		val p = points
		min = p.reduce { a, b -> Vec2(minOf(a.x, b.x), minOf(a.y, b.y))}
		max = p.reduce { a, b -> Vec2(maxOf(a.x, b.x), maxOf(a.y, b.y))}
	}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersection = Plane(Vec3(origin), Vec3(0f, 0f, 1f)).intersectedBy(ray).getOrNull(0) ?: return arrayOf()
		return if (containsPoint(Vec2(intersection))) arrayOf(intersection)
		else arrayOf()
	}

	override fun containsPoint(point: Vec2): Boolean {

		// https://stackoverflow.com/a/8862483

		// P0P is the vector from the origin on the plane to the intersection
		val P0P = point - this.origin

		// That vector is then projected onto the sides of the rect, if those projections are shorter than both sides
		// then the point is inside the rect
		val q1 = P0P projectOn side1
		val q2 = P0P projectOn side2

		// For both projections check that the projection is a shorter version of the rectangle side
		val q1b =
			((q1.x != 0f && side1.x / q1.x >= 1) || side1.x == 0f) && ((q1.y != 0f && side1.y / q1.y >= 1) || side1.y == 0f)
		val q2b =
			((q2.x != 0f && side2.x / q2.x >= 1) || side2.x == 0f) && ((q2.y != 0f && side2.y / q2.y >= 1) || side2.y == 0f)

		return q1b && q2b
	}

	// Same as Rect3D
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

	override fun getNormals(): Set<Vec2> = setOf(normal1, normal2)

	override fun projectToNormal(normal: Vec2): Set<Float> {
		return projectAllPoints(normal, points)
	}

	override fun getBoundingCircle(): Circle {
		return Circle(center, .5f * sqrt(length1 * length1 + length2 * length2))
	}

	override fun transformedBy(model: Mat4): Rect2D {
		val scale = Vec2(model.getScale())
		val rotation = model.getRotation().eulerAngles().z
		return Rect2D((origin.rotate(rotation) * scale) + Vec2(model.getTranslation()), lengths * scale, angle - rotation)
	}


	override fun toString(): String {
		return "Rect2D[$origin, $lengths]"
	}
}