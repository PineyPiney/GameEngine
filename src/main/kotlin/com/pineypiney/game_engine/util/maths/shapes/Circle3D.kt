package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.sqrt

class Circle3D(override val center: Vec3, val normal: Vec3, val radius: Float) : Shape3D() {

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val planeIntersection = Plane(center, normal).intersectedBy(ray).getOrNull(0) ?: return arrayOf()
		return if((planeIntersection - center).length2() > radius * radius) emptyArray()
		else arrayOf(planeIntersection)
	}

	override infix fun containsPoint(point: Vec3): Boolean {

		// https://stackoverflow.com/a/8862483

		// P0P is the vector from the origin on the plane to the intersection
		val P0P = point - center

		//https://stackoverflow.com/a/11132767
		val v1: Vec3 = perp(normal)
		val v2 = normal cross v1


		// That vector is then projected onto the sides of the circle, if those projections are shorter
		// than the radius when combined then the point is in the circle
		val q1 = P0P projectOn v1
		val q2 = P0P projectOn v2


		return (q1 + q2).length2() < radius * radius
	}

	// https://gamedev.stackexchange.com/a/169389
	override fun vectorTo(point: Vec3): Vec3 {
		val centerVec = point - center
		val a = sqrt(centerVec dot centerVec) - radius
		return if (a < 0) Vec3(0f)
		else centerVec.normalize() * a
	}

	override fun transformedBy(model: Mat4): Circle3D {
		// TODO("figure out scaling")
		return Circle3D(center + model.getTranslation(), normal.transformedBy(model.rotationComponent()), radius * model.getScale().x)
	}

	override fun getNormals(): Set<Vec3> {
		return emptySet()
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		// https://www.desmos.com/3d/hnmsj29dz1
		//val b = perp(this.normal)
		//val c = this.normal cross b

		val d1 = this.normal cross normal
		val d2 = (this.normal cross d1).normalize()

		val p1 = center + d2
		val p2 = center - d2

		val t1 = p1 dot normal
		val t2 = p2 dot normal

		return setOf(t1, t2)
	}

	override fun translate(move: Vec3) {
		center += move
	}

	fun perp(vec: Vec3): Vec3{
		return if(vec.z == 0f)  Vec3(0f, 0f, 1f)
		else {
			val vz = -(vec.x + vec.y) / vec.z
			Vec3(1f, 1f, vz) / sqrt(vz * vz + 2f)
		}
	}
}