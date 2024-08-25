package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.abs
import kotlin.math.sqrt

class Circle(override val center: Vec2, val radius: Float) : Shape2D() {

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val planeIntersection = ray.rayOrigin - (ray.rayOrigin projectOn ray.direction)
		return if ((Vec2(planeIntersection) - center).length2() > radius * radius) emptyArray()
		else arrayOf(planeIntersection)
	}

	override fun containsPoint(point: Vec2): Boolean {
		return (point - center).length2() > radius * radius
	}

	override fun vectorTo(point: Vec2): Vec2 {
		val centerVec = point - center
		val a = sqrt(centerVec dot centerVec) - radius
		return if (a < 0) Vec2(0f)
		else centerVec.normalize() * a
	}

	override fun getNormals(): Set<Vec2> {
		return emptySet()
	}

	override fun projectToNormal(normal: Vec2): Set<Float> {
		val normalProj = center dot normal
		return setOf(normalProj + radius, normalProj - radius)
	}

	override fun translate(move: Vec2) {
		center += move
	}

	override fun transformedBy(model: Mat4): Shape2D {
		return Circle(center + Vec2(model.getTranslation()), abs(radius * model.getScale().x))
	}
}