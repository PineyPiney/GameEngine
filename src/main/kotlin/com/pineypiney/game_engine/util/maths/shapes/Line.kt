package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Line(val start: Vec3, val end: Vec3) : Shape3D() {

	override val min: Vec3 = Vec3(minOf(start.x, end.x), minOf(start.y, end.y), minOf(start.z, end.z))
	override val max: Vec3 = Vec3(maxOf(start.x, end.x), maxOf(start.y, end.y), maxOf(start.z, end.z))

	val grad = (end - start).normalize()

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		return arrayOf()
	}

	override fun containsPoint(point: Vec3): Boolean {
		return false
	}

	override fun vectorTo(point: Vec3): Vec3 {
		val op = point - start
		val side = (end - start)

		val a = op dot (end - start)
		val x: Vec3 = if (a < 0) Vec3(0f)
		else if (a > side.length()) side
		else op projectOn side

		return start + x
	}

	override fun getNormals(): Set<Vec3> {
		return setOf(grad)
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return setOf(start dot normal, end dot normal)
	}

	override fun transformedBy(model: Mat4): Shape3D {
		val s = end - start
		val m = model.rotationComponent().scale(model.getScale())
		val newStart = start + model.getTranslation()
		val newS = Vec3(m * Vec4(s, 1f))
		return Line(newStart, newStart + newS)
	}
}