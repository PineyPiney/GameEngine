package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.extension_functions.removeNullValues
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec2.Vec2

class Collider2DComponent(parent: GameObject, var shape: Shape2D = Rect2D(0f, 0f, 1f, 1f), val flags: MutableSet<String> = mutableSetOf()) : Component(parent) {

	constructor(parent: GameObject, shape: Shape2D, vararg flags: String): this(parent, shape, mutableSetOf(*flags))

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

	fun checkAllCollisionsNew(movement: Vec2, endMovement: Vec2, stepBias: Vec2 = Vec2(0f)): Vec2 {

		val remainingMovement = Vec2(movement)
		val collidedMove = Vec2(0f)
		val moveDist = movement.length()

		// Create a temporary collision box to move around for collision checks
		val newCollision = transformedShape

		val circle = newCollision.getBoundingCircle()
		val nearbyColliders = parent.objects?.getAll2DCollisions()?.filter {
			it.active && it != this && it.transformedShape.getBoundingCircle().distanceTo(circle) < moveDist
		} ?: return remainingMovement

		// Iterate over all collision boxes sharing object collections and
		// eject this collision boxes object if the collision boxes collide
		while(remainingMovement.length2() > 0f){
			// Move the shape to the new position
			newCollision translate remainingMovement
			// Get all collisions with nearby colliders
			val allCollisions = nearbyColliders.associateWith{ newCollision.calculateCollision(it.transformedShape, remainingMovement, stepBias) }.removeNullValues()
			// Get the first collision
			val (collider, collision) = allCollisions.maxByOrNull { it.value.removeShape1FromShape2.length2() } ?: break

			// Adjust the movement that will be returned by the functions and move
			// the testing collider to where it would be stopped by this collision
			collidedMove plusAssign (remainingMovement + collision.removeShape1FromShape2)
			newCollision translate collision.removeShape1FromShape2
			// Collision callbacks
			parent.getComponent<Collider2DCallback>()?.onCollide(this, collider, collision.removeShape1FromShape2)
			collider.parent.getComponent<Collider2DCallback>()?.onCollide(collider, this, collision.removeShape1FromShape2)

			// Get the new movement from the collider "sliding" along the collision tangent
			val newMovement = collision.collisionNormal.normal()
			if(newMovement dot remainingMovement < 0f) newMovement(-newMovement)

			val moveCos = newMovement dot movement
			// If the new movement is facing against the original movement then stop moving
			if(moveCos < 0f) {
				endMovement(0f, 0f)
				break
			}

			val remainingDistance = collision.removeShape1FromShape2.length()
			remainingMovement(newMovement * remainingDistance)

			endMovement(movement projectOn newMovement)
		}

		return collidedMove + remainingMovement
	}

	fun checkAllCollisions(movement: Vec2, stepBias: Vec2 = Vec2(0f)): Vec2 {

		val collidedMove = Vec2(movement)
		var moveDist = movement.length()

		// Create a temporary collision box in the new position to calculate collisions
		val newCollision = transformedShape
		newCollision translate movement

		val circle = newCollision.getBoundingCircle()
		val nearbyColliders = parent.objects?.getAll2DCollisions()?.filter {
			it.active && it != this && it.transformedShape.getBoundingCircle().distanceTo(circle) < moveDist
		} ?: return collidedMove

		// Iterate over all collision boxes sharing object collections and
		// eject this collision boxes object if the collision boxes collide
		for (collider in nearbyColliders) {
			val overlap = newCollision.getEjection(collider.transformedShape, movement, stepBias)
			if(overlap.x == 0f && overlap.y == 0f) continue

			collidedMove plusAssign overlap
			newCollision translate overlap
			parent.getComponent<Collider2DCallback>()?.onCollide(this, collider, overlap)
			collider.parent.getComponent<Collider2DCallback>()?.onCollide(collider, this, -overlap)
		}

		return collidedMove
	}

	fun isGrounded(): Boolean {
		val b = transformedShape
		b translate Vec2(0f, -0.01f)

		return (parent.objects?.getAll2DCollisions()?.minus(this))?.any { it.transformedShape.intersects(b) } ?: false
	}
}