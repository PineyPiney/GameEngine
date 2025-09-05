package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.reduceFieldsOrNull
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class CompoundShape3D(val shapes: MutableSet<Shape3D>) : Shape3D() {

	override val center: Vec3
		get() {
			val c = Vec3()
			for (s in shapes) c += s.center
			return c / shapes.size
		}

	override val min: Vec3 = shapes.reduceFieldsOrNull(Shape3D::min) { a, b -> Vec3(minOf(a.x, b.x), minOf(a.y, b.y), minOf(a.z, b.z))} ?: Vec3()
	override val max: Vec3 = shapes.reduceFieldsOrNull(Shape3D::max) { a, b -> Vec3(maxOf(a.x, b.x), maxOf(a.y, b.y), maxOf(a.z, b.z))} ?: Vec3()

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersections = mutableSetOf<Vec3>()
		for (shape in shapes) intersections.addAll(shape intersectedBy ray)
		return intersections.toTypedArray()
	}

	override fun containsPoint(point: Vec3): Boolean {
		return shapes.any { it.containsPoint(point) }
	}

	// Same as Rect3D with extra dimension
	override fun vectorTo(point: Vec3): Vec3 {
		return shapes.minOfWith({ f, s -> f.length2().compareTo(s.length()) }, { it vectorTo point })
	}

	override fun transformedBy(model: Mat4): CompoundShape3D {
		return CompoundShape3D(shapes.map { it.transformedBy(model) }.toMutableSet())
	}

	override fun getNormals(): Set<Vec3> {
		return shapes.flatMap { it.getNormals() }.toSet()
	}

	override fun projectToNormal(normal: Vec3): Set<Float> {
		return shapes.flatMap { it.projectToNormal(normal) }.toSet()
	}

	override fun translate(move: Vec3) {
		for (shape in shapes) shape.translate(move)
	}
}