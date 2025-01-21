package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.shapes.Shape3D
import glm_.vec3.Vec3

open class Collider3DComponent(parent: GameObject, open val shape: Shape3D) : Component(parent, "C3D") {

	val transformedShape get() = shape transformedBy parent.worldModel
	var active = true

	infix fun collidesWith(other: Collider3DComponent): Boolean {
		return this.parent != other.parent && active && other.active && shape intersects other.shape
	}

	fun isColliding(collisions: Collection<Collider3DComponent>? = parent.objects?.getAll3DCollisions()): Boolean {
		if (collisions.isNullOrEmpty()) return false
		for (c in collisions.toSet()) if (this collidesWith c) return true
		return false
	}

	fun checkAllCollisions(movement: Vec3, stepBias: Vec3? = null): Vec3 {

		val collidedMove = movement.copy()

		// Create a temporary collision box in the new position to calculate collisions
		val newCollision = transformedShape
		newCollision.translate(movement)

		// Iterate over all collision boxes sharing object collections and
		// eject this collision boxes object if the collision boxes collide
		for (collider in parent.objects?.getAll3DCollisions() ?: emptySet()) {
			if (collider != this) {
				val overlap = newCollision.getEjection(collider.transformedShape, movement, stepBias)
				if (overlap.x != 0f || overlap.y != 0f || overlap.z != 0f) {
					newCollision.translate(overlap)
					collidedMove plusAssign overlap
				}
			}
		}

		return collidedMove
	}

	fun isGrounded(): Boolean {
		val b = transformedShape
		b.translate(Vec3(0f, -0.01f, 0f))

		return (parent.objects?.getAll3DCollisions()?.minus(this))?.any { it.transformedShape.intersects(b) } ?: false
	}
}