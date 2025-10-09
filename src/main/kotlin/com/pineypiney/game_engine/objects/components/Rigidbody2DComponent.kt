package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.util.extension_functions.normal
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.abs

class Rigidbody2DComponent(parent: GameObject, var mass: Float = 1f, var friction: Float = 1f, var dragCoefficient: Float = 1.5f) : Component(parent), UpdatingComponent {


	var velocity = Vec2()
	var acceleration = Vec2()

	var gravity = Vec2(0f, -9.81f)

	var stepBias: Vec2 = Vec2(0f)

	override fun update(interval: Float) {
		// If no time has passed then don't bother doing anything
		if(interval == 0f) return
		val collider = parent.getComponent<Collider2DComponent>()

		acceleration plusAssign gravity
		if(collider != null && velocity != Vec2(0f)) {
			applyForce(calculateResistiveForces(collider, interval))
		}

		// If the acceleration reverses the direction of movement then still the object
		val oldVelocity = Vec2(velocity)
		velocity plusAssign acceleration * interval
		if(velocity dot oldVelocity < 0f) velocity = Vec2(0f)

		if(velocity != Vec3(0f)) {
			val movement = velocity * interval
			if (collider?.active == true) {
				val endMovement = Vec2(movement)
				val collidedMove = collider.checkAllCollisions(movement, endMovement, stepBias)
				parent.translate(Vec3(collidedMove, 0f))

				velocity = endMovement / interval
			} else parent.translate(Vec3(movement, 0f))
		}

		acceleration = Vec2(0f)
	}

	fun calculateResistiveForces(collider: Collider2DComponent, interval: Float): Vec2{
		val rawForce = calculateFriction(collider, interval) + calculateAirResistance(collider, interval)

		// If the friction of going to push the overall velocity change this update to be greater than the current velocity,
		// then it will start this body going backwards, which is not how friction works.
		return if((acceleration * interval).length2() <= velocity.length2() && ((acceleration + rawForce/mass) * interval).length2() > velocity.length2()){
			-(acceleration + (velocity / interval))
		} else rawForce
	}

	fun calculateFriction(collider: Collider2DComponent, interval: Float): Vec2{
		val movement = (velocity + acceleration * interval).normalize() * .01f

		// Find a collision box that this body might be grounded on,
		// if there is none then no friction should be applied
		val ground = collider.isGrounded(movement) ?: return Vec2(0f)
		val shape = collider.transformedShape transformedBy Mat4().translate(Vec3(movement))
		val normal = shape.calculateCollision(ground.transformedShape, movement)?.collisionNormal ?: return Vec2()

		// Find the magnitude of the normal force against the other collider
		val force = acceleration * mass
		val normalForce = abs(force dot normal)

		// Friction acts opposite to the direction of motion
		val frictionDir = -velocity.normalize()
		// Friction force is the direction multiplied by the normal force magnitude and the friction coefficients of both bodies
		return frictionDir * normalForce * friction * (ground.parent.getComponent<Rigidbody2DComponent>()?.friction ?: 1f)
	}

	fun calculateAirResistance(collider: Collider2DComponent, interval: Float): Vec2{
		// This is the length of object pushing against air based on it's movement
		val shapeProjection = collider.shape.projectTo(velocity.normal().normalize()).let { abs(it.y - it.x) }

		val vel = (velocity + (acceleration * interval))
		val speed2 = vel.length2()
		return -vel.normalize() * speed2 * dragCoefficient * shapeProjection
	}

	fun applyForce(force: Vec2){
		acceleration plusAssign (force / mass)
	}
}