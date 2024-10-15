package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.reduceA
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2Vars
import glm_.vec3.Vec3

abstract class Shape<V : Vec2Vars<Float>> {

	abstract val center: V

	/**
	 * Get the maximum and minimum points of this shape along the vector [normal]
	 *
	 * @param normal The vector to project the shape onto
	 *
	 * @return A vec2 where the first float is the minimum distance along the line,
	 * including negative values, and the second is the maximum distance
	 */
	infix fun projectTo(normal: V): Vec2 {
		val points = projectToNormal(normal)
		return if (points.isEmpty()) Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
		else points.reduceA(Vec2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) { acc, p ->
			Vec2(kotlin.math.min(acc.x, p), kotlin.math.max(acc.y, p))
		}
	}

	/**
	 * Returns all points in 3D space where [ray] intersects this shape
	 *
	 * @param ray The ray to check intersects this shape
	 *
	 * @return An array of Vec3 with points where the ray intersects
	 */
	abstract infix fun intersectedBy(ray: Ray): Array<Vec3>

	/**
	 * Check if this shape contains a point in this shape's dimensional space
	 *
	 * @param point The position of the point in this shape's dimensions
	 *
	 * @return Whether this shape contains [point]
	 */
	abstract infix fun containsPoint(point: V): Boolean
	/**
	 * The shortest vector from the edge of this shape to [point]
	 *
	 * @param point The point to find a vector to
	 *
	 * @return The shortest vector to [point]
	 */
	abstract infix fun vectorTo(point: V): V

	/**
	 * Get the normals of this shape in the dimensions of the shape
	 *
	 * @return A Set of normals
	 */
	abstract fun getNormals(): Set<V>

	/**
	 * Project this shape onto [normal], and get all relevant distances along that normal
	 *
	 * @param normal The vector to project the shape onto
	 *
	 * @return A Set containing all the distances
	 */
	abstract infix fun projectToNormal(normal: V): Set<Float>

	/**
	 * Translate this shape by [move]
	 *
	 * @param move The vector to translate this shape by
	 */
	abstract infix fun translate(move: V)

	/**
	 * Transform the shape by [model]
	 *
	 * @param model The matrix to transform the shape by
	 *
	 * @return A new shape transformed by [model]
	 */
	abstract infix fun transformedBy(model: Mat4): Shape<*>
}