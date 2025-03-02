package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.reduceFields
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class CompoundShape2D(val shapes: MutableSet<Shape2D>) : Shape2D() {

	constructor(vararg shapes: Shape2D): this(shapes.toMutableSet())

	override val center: Vec2
		get() {
			val c = Vec2()
			for (s in shapes) c += s.center
			return c / shapes.size
		}

	override val min: Vec2 = shapes.reduceFields(Shape2D::min, Vec2::min)
	override val max: Vec2 = shapes.reduceFields(Shape2D::max, Vec2::max)

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		val intersections = mutableSetOf<Vec3>()
		for (shape in shapes) intersections.addAll(shape intersectedBy ray)
		return intersections.toTypedArray()
	}

	override fun containsPoint(point: Vec2): Boolean {
		return shapes.any { it.containsPoint(point) }
	}

	// Same as Rect3D with extra dimension
	override fun vectorTo(point: Vec2): Vec2 {
		return shapes.minOfWith({ f, s -> f.length2().compareTo(s.length()) }, { it vectorTo point })
	}

	override fun transformedBy(model: Mat4): CompoundShape2D {
		return CompoundShape2D(shapes.map { it.transformedBy(model) }.toMutableSet())
	}

	override fun getNormals(): Set<Vec2> {
		return shapes.flatMap { it.getNormals() }.toSet()
	}

	override fun projectToNormal(normal: Vec2): Set<Float> {
		return shapes.flatMap { it.projectToNormal(normal) }.toSet()
	}

	override fun getBoundingCircle(): Circle {
		return Circle((min + max) * .5f, (max - min).length() * .5f)
	}

	override fun translate(move: Vec2) {
		for (shape in shapes) shape.translate(move)
	}
}