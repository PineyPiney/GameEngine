package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.reduceA
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2Vars
import glm_.vec3.Vec3

abstract class Shape<V : Vec2Vars<Float>> {

	abstract val center: V

	infix fun projectTo(normal: V): Vec2 {
		val points = projectToNormal(normal)
		return if (points.isEmpty()) Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
		else points.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, p ->
			Vec2(kotlin.math.min(acc.x, p), kotlin.math.max(acc.y, p))
		}
	}

	abstract infix fun intersectedBy(ray: Ray): Array<Vec3>
	abstract infix fun containsPoint(point: V): Boolean
	abstract infix fun vectorTo(point: V): V

	abstract fun getNormals(): Set<V>
	abstract infix fun projectToNormal(normal: V): Set<Float>
	abstract infix fun translate(move: V)

	abstract infix fun transformedBy(model: Mat4): Shape<*>
}