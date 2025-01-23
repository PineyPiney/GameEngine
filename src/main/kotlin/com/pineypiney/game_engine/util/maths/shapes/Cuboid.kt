package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.operators.div
import kotlin.math.abs

class Cuboid(override var center: Vec3, var rotation: Quat, var sides: Vec3) : Shape3D() {

	override val min: Vec3
	override val max: Vec3

	init {
		val p = points
		min = p.reduce { a, b -> Vec3(minOf(a.x, b.x), minOf(a.y, b.y), minOf(a.z, b.z))}
		max = p.reduce { a, b -> Vec3(maxOf(a.x, b.x), maxOf(a.y, b.y), maxOf(a.z, b.z))}
	}

	val side1 get() = Vec3(sides.x, 0f, 0f).rotate(rotation)
	val side2 get() = Vec3(0f, sides.y, 0f).rotate(rotation)
	val side3 get() = Vec3(0f, 0f, sides.z).rotate(rotation)

	val points: Array<Vec3>
		get() {
			val sides = arrayOf(side1, side2, side3)
			val o = center - (this.sides.rotate(rotation) * .5f)
			return Array(8) {
				val v = Vec3(o)
				for (i in 0..2) if (it and (1 shl i) > 0) v += sides[i]
				v
			}
		}

	override fun intersectedBy(ray: Ray): Array<Vec3> {

		val rotationMatrix = rotation.toMat4().inverse()
		val rotatedOrigin = ray.rayOrigin transformedBy rotationMatrix
		val rotatedDir = ray.direction transformedBy rotationMatrix

		val rotatedCenter = center transformedBy rotationMatrix
		val rotatedMin = rotatedCenter - (sides / 2)
		val rotatedMax = rotatedMin + sides

		val invMag = 1f / rotatedDir
		// The distances of the ray to the boundaries of the box,
		// scaled to the direction of the ray, so it can be considered
		// the number of steps the ray must take to reach the boundary
		val stepsToMin = (rotatedMin - rotatedOrigin) * invMag
		val stepsToMax = (rotatedMax - rotatedOrigin) * invMag

		val (minX, maxX) = if (stepsToMin.x < stepsToMax.x) Vec2(stepsToMin.x, stepsToMax.x) else Vec2(
			stepsToMax.x,
			stepsToMin.x
		)
		val (minY, maxY) = if (stepsToMin.y < stepsToMax.y) Vec2(stepsToMin.y, stepsToMax.y) else Vec2(
			stepsToMax.y,
			stepsToMin.y
		)
		val (minZ, maxZ) = if (stepsToMin.z < stepsToMax.z) Vec2(stepsToMin.z, stepsToMax.z) else Vec2(
			stepsToMax.z,
			stepsToMin.z
		)

		val lastEnter = maxOf(minX, minY, minZ)
		val firstExit = minOf(maxX, maxY, maxZ)

		return if (0f < lastEnter && lastEnter < firstExit) arrayOf(
			rotatedOrigin + (rotatedDir * lastEnter),
			rotatedOrigin + (rotatedDir * firstExit)
		)
		else arrayOf()
	}

	override fun containsPoint(point: Vec3): Boolean {
		val vec = point - center

		val project1 = vec projectOn side1.normalize()
		val project2 = vec projectOn side2.normalize()
		val project3 = vec projectOn side3.normalize()

		return project1.length() <= abs(sides.x) * .5f &&
				project2.length() <= abs(sides.y) * .5f &&
				project3.length() <= abs(sides.z) * .5f
	}

	// Same as Rect3D with extra dimension
	override fun vectorTo(point: Vec3): Vec3 {
		val origin = center - sides.rotate(rotation)
		val side1 = side1
		val side2 = side2
		val side3 = side3

		val op = point - origin

		val a = op dot side1
		val x: Vec3 = if (a < 0) Vec3(0f)
		else if (a > side1.length()) side1
		else op projectOn side1

		val b = op dot side2
		val y: Vec3 = if (b < 0) Vec3(0f)
		else if (b > side2.length()) side2
		else op projectOn side2

		val c = op dot side3
		val z: Vec3 = if (c < 0) Vec3(0f)
		else if (c > side3.length()) side3
		else op projectOn side3

		val closestPoint = origin + x + y + z
		return point - closestPoint
	}

	override fun getNormals(): Set<Vec3> {
		return setOf(side1.normalize(), side2.normalize(), side3.normalize())
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return projectAllPoints(normal, points.toSet())
	}

	override fun translate(move: Vec3) {
		center plusAssign move
	}

	override fun transformedBy(model: Mat4): Cuboid {
		val scale = model.getScale()
		val rotation = model.getRotation()
		//return Rect2D((origin.rotate(rotation) * scale) + Vec2(model.getTranslation()), size * scale, angle - rotation)
		return Cuboid(center.rotate(rotation) * scale + model.getTranslation(), this.rotation * rotation, sides * scale)
	}
}