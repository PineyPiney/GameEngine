package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.maxOf
import com.pineypiney.game_engine.util.extension_functions.minOf
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.abs

class TriangulatedSolid(val triangles: Set<Triangle3D>) : Shape3D() {

	override val min: Vec3
	override val max: Vec3

	init {
		val mi = Vec3(Float.MAX_VALUE)
		val ma = Vec3(-Float.MAX_VALUE)
		for(tri in triangles){
			mi(minOf(mi, tri.min))
			ma(maxOf(ma, tri.max))
		}
		min = mi
		max = ma
	}

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersections = mutableSetOf<Vec3>()
		for(tri in triangles){
			val triInts = tri.intersectedBy(ray)
			for(int in triInts){
				if(intersections.none {
					abs(it.x - int.x) > 1e-4f &&
					abs(it.y - int.y) > 1e-4f &&
					abs(it.z - int.z) > 1e-4f
				}) intersections.add(int)
			}
		}
		return intersections.toTypedArray()
	}

	override fun containsPoint(point: Vec3): Boolean {
		val p = min - Vec3(1f)
		val ray = Ray(p, point)
		val intersections = intersectedBy(ray)
		val pointDistance = (point - p).length()
		val intersectBefore = intersections.count { (it - p).length() < pointDistance}
		return intersectBefore % 2 == 1
	}

	override fun vectorTo(point: Vec3): Vec3 {
		return if(containsPoint(point)) Vec3(0f)
		else triangles.map { it.vectorTo(point) }.minBy { it.length2() }
	}

	override fun getNormals(): Set<Vec3> {
		return triangles.flatMap { it.getNormals() }.toSet()
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return triangles.flatMap { it.projectToNormal(normal) }.toSet()
	}

	override fun transformedBy(model: Mat4): Shape3D {
		return TriangulatedSolid(triangles.map { it.transformedBy(model) }.toSet())
	}
}