package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.objects.components.colliders.Collision2D
import com.pineypiney.game_engine.util.extension_functions.absMinOf
import glm_.func.common.abs
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import kotlin.math.abs

abstract class Shape2D : Shape<Vec2>() {

	override val size: Vec2 get() = max - min

	abstract override fun transformedBy(model: Mat4): Shape2D

	/**
	 * Gets the size of the overlap between two shapes in the direction of the given normal
	 *
	 * @param [normal] The direction to find the overlap in
	 * @param [other] The other shape to check against
	 *
	 * @return A Vec2 containing the overlap in the positive and negative directions of the vector respectively
	 *          If this shape is moved by either (normal * x) or (normal * y) it will no longer be intersecting with other
	 */
	fun overlap1D(normal: Vec2, other: Shape2D): Vec2 {

		// The range of the normal that each shape takes up
		val range1 = projectTo(normal)
		val range2 = other projectTo normal

		// If the two ranges don't overlap then return 0
		if (range1.x >= range2.y || range2.x >= range1.y) return Vec2(0f)
		// Otherwise pass the distances needed to separate the shapes in each direction,
		// where the first value is positive and the second is negative
		val a = range2.x - range1.y
		val b = range2.y - range1.x
		return if (a > 0f) Vec2(a, b) else Vec2(b, a)
	}

	// https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	open infix fun intersects(other: Shape2D): Boolean {
		return getNormals().all { overlap1D(it, other).x != 0f } &&
				other.getNormals().all { overlap1D(it, other).x != 0f } &&
				overlap1D((center - other.center).normalize(), other).x != 0f
	}

	abstract fun getBoundingCircle(): Circle

	fun calculateCollision(other: Shape2D, movement: Vec2, stepBias: Vec2 = Vec2(0f)): Collision2D?{
		val still = movement == Vec2(0f)
		val normals = (getNormals() + other.getNormals() + (center - other.center).normalize()).toSet()
		val lengths = normals.associateWith {
			// The overlap in each direction of the normal
			val overlaps = overlap1D(it, other)

			// If any of the normals don't overlap then the shapes also don't overlap so no collision occurs
			if (overlaps.x == 0f) return null

			// If there is no movement then just pick the smallest movement
			if (still) {
				absMinOf(overlaps.x, overlaps.y)
			}
			// Otherwise pick the one to move back against the original movement
			else {
				val dot = it dot movement
				// If the movement is in the other direction to the normal then use the positive magnitude (x)
				// otherwise use the negative version
				overlaps.run { if (dot < 0f) x else y }
			}
		}

		//if(lengths.any { it.value == 0f }) return Vec3(0f)

		if (!still) {
			if (stepBias != Vec2(0f)) {
				val stepMag = stepBias.length()
				for ((normal, mult) in lengths) {
					val dot = normal dot stepBias

					if (abs(dot) > stepMag * .9f && abs(mult) <= stepMag) return Collision2D(this, movement, other, Vec2(0f), Vec2(0f), normal, normal * mult)// TODO calculate collision point
				}
			}

			// If there is movement then move the smallest amount necessary back along that movement vector
			val moveMag = movement.length()
			var smallestEjection = Float.MAX_VALUE
			var collisionNormal = Vec2(0f)
			for ((normal, mult) in lengths) {
				val cosT = -(normal dot movement) / moveMag
				val ejectSize = mult / cosT
				if(ejectSize > 0f && ejectSize < smallestEjection) {
					smallestEjection = ejectSize
					collisionNormal = normal
				}
			}
			return Collision2D(this, movement, other, Vec2(0f), Vec2(0f), collisionNormal, -movement * smallestEjection / moveMag)
		}
		val (normal, mult) = lengths.minBy { it.value.abs }
		return Collision2D(this, movement, other, Vec2(0f), Vec2(0f), normal, normal * mult)
	}

	/**
	 * Determine the most appropriate ejection vector to move this shape out of other
	 *
	 * @param other The shape to eject this shape from
	 * @param movement The original movement of this
	 * @param stepBias If there is a direction that this should be able to 'step' up.
	 *          Setting this to (0, 0.2, 0) would allow a player to step up a height increase of .2 in the y direction
	 *
	 * @return Returns the offset to movement to stop this from intersecting with other
	 */
	fun getEjection(other: Shape2D, movement: Vec2, stepBias: Vec2 = Vec2(0f)): Vec2 {
		val still = movement == Vec2(0f)
		val lengths = (getNormals() + other.getNormals() + (center - other.center).normalize()).associateWith {
			// The overlap in each direction of the normal
			val overlaps = overlap1D(it, other)

			// If any of the normals don't overlap then the shapes also don't overlap so no escape vector is needed
			if (overlaps.x == 0f) return Vec2(0f)

			// If there is no movement then just pick the smallest movement
			if (still) {
				absMinOf(overlaps.x, overlaps.y)
			}
			// Otherwise pick the one to move back against the original movement
			else {
				val dot = it dot movement
				// If the movement is in the other direction to the normal then use the positive magnitude (x)
				// otherwise use the negative version
				overlaps.run { if (dot < 0f) x else y }
			}
		}

		//if(lengths.any { it.value == 0f }) return Vec3(0f)

		if (!still) {
			if (stepBias != Vec2(0f)) {
				val stepMag = stepBias.length()
				for ((normal, mult) in lengths) {
					val dot = normal dot stepBias

					if (abs(dot) > stepMag * .9f && abs(mult) <= stepMag) return normal * mult
				}
			}

			// If there is movement then check for a vector parallel to the movement, which would be used automatically
			val moveMag = movement.length()
			for ((normal, mult) in lengths) {
				val dot = normal dot movement
				// if the normal is nearly parallel to the movement, and the new movement is smaller that the original movement
				// This almost always means someone's walking into a wall, falling into a floor etc.
				if (abs(dot) > moveMag * .9f && abs(mult) <= moveMag) return normal * mult
			}
		}
		val r = lengths.minBy { it.value.abs }.run { key * value }
		return r
	}

	companion object {
		fun projectAllPoints(normal: Vec2, points: Set<Vec2>): Set<Float> {
			return points.map { it dot normal }.toSet()
		}
	}
}