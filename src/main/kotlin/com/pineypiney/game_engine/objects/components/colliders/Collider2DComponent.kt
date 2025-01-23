package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec2.Vec2

class Collider2DComponent(parent: GameObject, var shape: Shape2D, val flags: MutableSet<String> = mutableSetOf()) : Component(parent) {

	constructor(parent: GameObject, shape: Shape2D, vararg flags: String): this(parent, shape, mutableSetOf(*flags))

	constructor(parent: GameObject) : this(parent, Rect2D(Vec2(), Vec2(1)))

	val transformedShape get() = shape transformedBy parent.worldModel
	var active = true

	infix fun collidesWith(other: Collider2DComponent): Boolean {
		return this.parent != other.parent && active && other.active && shape intersects other.shape
	}

	fun isColliding(collisions: Collection<Collider2DComponent>? = parent.objects?.getAll2DCollisions()): Boolean {
		if (collisions.isNullOrEmpty()) return false
		for (c in collisions.toSet()) if (this collidesWith c) return true
		return false
	}

	fun checkAllCollisions(movement: Vec2, stepBias: Vec2? = null): Vec2 {

		val collidedMove = movement.copy()

		// Create a temporary collision box in the new position to calculate collisions
		val newCollision = transformedShape
		newCollision translate movement

		// Iterate over all collision boxes sharing object collections and
		// eject this collision boxes object if the collision boxes collide
		for (collider in parent.objects?.getAll2DCollisions() ?: emptySet()) {
			if (collider.active && collider != this) {
				val overlap = newCollision.getEjection(collider.transformedShape, movement, stepBias)
				if (overlap.x != 0f || overlap.y != 0f) {
					collidedMove plusAssign overlap
					newCollision translate overlap
					parent.getComponent<Collider2DCallback>()?.onCollide(this, collider, overlap)
					collider.parent.getComponent<Collider2DCallback>()?.onCollide(collider, this, -overlap)
				}
			}
		}

		return collidedMove
	}

	fun isGrounded(): Boolean {
		val b = transformedShape
		b translate Vec2(0f, -0.01f)

		return (parent.objects?.getAll2DCollisions()?.minus(this))?.any { it.transformedShape.intersects(b) } ?: false
	}
}