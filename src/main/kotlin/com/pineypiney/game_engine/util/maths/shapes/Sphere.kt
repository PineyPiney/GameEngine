package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.sqrt

class Sphere(override val center: Vec3, val radius: Float): Shape3D(){

	override fun transformedBy(model: Mat4): Sphere {
		return Sphere(center + model.getTranslation(), radius * model.getScale().x)
	}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection.html
		val l = center - ray.rayOrigin
		val tca = l dot ray.direction
		val d2 = l.length2() + tca*tca

		val r2 = radius * radius
		if(d2 < r2) return emptyArray()

		val thc = sqrt(r2 - d2)
		return arrayOf(ray.rayOrigin + (ray.direction * (tca - thc)), ray.rayOrigin + ray.direction * (tca + thc))
	}

	override fun containsPoint(point: Vec3): Boolean {
		return (center - point).length2() <= radius * radius
	}

	override fun vectorTo(point: Vec3): Vec3 {
		val vec = point - center
		val dist = vec.length()
		return if(dist <= radius) Vec3(0f) else vec * (1f - (radius / dist))
	}

	override fun getNormals(): Set<Vec3> {
		return emptySet()
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		val centerPoint = center dot normal
		return setOf(centerPoint + radius, centerPoint - radius)
	}

	override fun translate(move: Vec3) {
		center += move
	}
}