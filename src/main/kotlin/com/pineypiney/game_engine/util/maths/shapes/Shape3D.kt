package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.absMinOf
import com.pineypiney.game_engine.util.extension_functions.reduceA
import glm_.func.common.abs
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.abs

abstract class Shape3D: Shape() {

	abstract override fun transformedBy(model: Mat4): Shape3D

	abstract fun getNormals(): Set<Vec3>
	abstract fun projectToNormal(normal: Vec3): Set<Vec3>
	abstract fun translate(move: Vec3)

	infix fun projectTo(normal: Vec3): Vec2 {
		val points = projectToNormal(normal)
		return if(points.isEmpty()) Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
			else points.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, pp ->
			// Normalise pp twice because of floating point errors
			val p = pp.length() * if (pp.normalize().normalize() != normal.normalize()) -1 else 1
			Vec2(kotlin.math.min(acc.x, p), kotlin.math.max(acc.y, p))
		}
	}

	/**
	 * Gets the size of the overlap between two shapes in the direction of the given normal
	 *
	 * @param [normal] The direction to find the overlap in
	 * @param [other] The other shape to check against
	 *
	 * @return A Vec2 containing the overlap in the positive and negative directions of the vector respectively
	 *          If this shape is moved by either (normal * x) or (normal * y) it will no longer be intersecting with other
	 */
	fun overlap1D(normal: Vec3, other: Shape3D): Vec2 {

		// The range of the normal that each rect takes up
		val range1 = projectTo(normal)
		val range2 = other projectTo normal

		// If the two ranges don't overlap then return 0
		if (range1.x >= range2.y || range2.x >= range1.y) return Vec2(0f)
		// Otherwise pass the distances needed to separate the shapes in each direction,
		// where the first value is positive and the second is negative
		val a = range2.x - range1.y
		val b = range2.y - range1.x
		return if(a > 0f) Vec2(a, b) else Vec2(b, a)
	}

	infix fun intersects(other: Shape3D): Boolean{
		return (getNormals() + other.getNormals()).all { overlap1D(it, other).x != 0f }
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
	fun getEjection(other: Shape3D, movement: Vec3, stepBias: Vec3? = null): Vec3{
		val still = movement == Vec3(0f)
		val lengths = (getNormals() + other.getNormals()).associateWith {
			// The overlap in each direction of the normal
			val overlaps = overlap1D(it, other)

			// If any of the normals don't overlap then the shapes also don't overlap so no escape vector is needed
			if(overlaps.x == 0f) return Vec3(0f)

			// If there is no movement then just pick the smallest movement
			if (still) {
				absMinOf(overlaps.x, overlaps.y)
			}
			// Otherwise pick the one to move back against the original movement
			else{
				val dot = it dot movement
				// If the movement is in the other direction to the normal then use the positive magnitude (x)
				// otherwise use the negative version
				overlaps.run { if (dot < 0f) x else y }
			}
		}

		//if(lengths.any { it.value == 0f }) return Vec3(0f)

		if(!still) {
			if(stepBias != null) {
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
}