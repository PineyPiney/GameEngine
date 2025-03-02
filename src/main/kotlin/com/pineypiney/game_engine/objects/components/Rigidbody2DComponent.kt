package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Rigidbody2DComponent(parent: GameObject) : Component(parent), UpdatingComponent {

	var velocity = Vec2()
	var acceleration = Vec2()

	var gravity = Vec2(0f, -9.81f)

	var stepBias: Vec2 = Vec2(0f)

	override fun update(interval: Float) {
		// If no time has passed then don't bother doing anything
		if(interval == 0f) return

		acceleration plusAssign gravity
		velocity plusAssign acceleration * interval
		val collider = parent.getComponent<Collider2DComponent>()
		val movement = velocity * interval
		if (collider != null && collider.active) {
			val endMovement = Vec2(movement)
			val collidedMove = collider.checkAllCollisionsNew(movement, endMovement, stepBias)
			parent.translate(Vec3(collidedMove, 0f))

			velocity = endMovement / interval
		} else parent.translate(Vec3(movement, 0f))

		acceleration = Vec2(0f)
	}
}