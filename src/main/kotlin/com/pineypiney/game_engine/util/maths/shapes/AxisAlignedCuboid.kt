package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.operators.div

class AxisAlignedCuboid(val center: Vec3, val sides: Vec3) : Shape3D() {

	override val min = center - (sides / 2)
	override val max = min + sides

	val points: Array<Vec3>
		get() {
			return Array(8) {
				center + (Vec3(
					if (it >= 4) sides.x else -sides.x,
					if ((it % 4) >= 2) sides.y else -sides.y,
					if ((it % 2) == 1) sides.z else -sides.z
				) * .5f)
			}
		}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val invMag = 1f / ray.direction

		// The distances of the ray to the boundaries of the box,
		// scaled to the direction of the ray, so it can be considered
		// the number of steps the ray must take to reach the boundary
		val stepsToMin = (min - ray.rayOrigin) * invMag
		val stepsToMax = (max - ray.rayOrigin) * invMag

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

		return if (lastEnter < firstExit) arrayOf(
			ray.rayOrigin + (ray.direction * lastEnter),
			ray.rayOrigin + (ray.direction * firstExit)
		)
		else arrayOf()
	}

	override fun containsPoint(point: Vec3): Boolean {

		if (min.x > point.x || point.x > max.x) return false
		if (min.y > point.y || point.y > max.y) return false
		return (min.z < point.z && point.z < max.z)
	}

	override fun transformedBy(model: Mat4): Shape3D {
		val scale = model.getScale()
		val rotation = model.getRotation()
		//return Rect2D((origin.rotate(rotation) * scale) + Vec2(model.getTranslation()), size * scale, angle - rotation)
		return Cuboid(center.rotate(rotation) * scale + model.getTranslation(), rotation, sides * scale)
	}

	override fun vectorTo(point: Vec3): Vec3 {
		return if (containsPoint(point)) Vec3(0f)
		else Vec3(
			absMinOf(point.x - min.x, point.x - max.x),
			absMinOf(point.y - min.y, point.y - max.y),
			absMinOf(point.z - min.z, point.z - max.z),
		)
	}

	override fun getNormals(): Set<Vec3> {
		return setOf(Vec3(1f, 0f, 0f), Vec3(0f, 1f, 0f), Vec3(0f, 0f, 1f))
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return projectAllPoints(normal, points.toSet())
	}
}