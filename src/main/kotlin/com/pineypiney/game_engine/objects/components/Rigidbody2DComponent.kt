package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Rigidbody2DComponent(parent: GameObject) : Component(parent), UpdatingComponent {

	var velocity = Vec2()
	var acceleration = Vec2()

	var gravity = Vec2(0f, -9.81f)

	var stepBias: Vec2? = null

	override fun update(interval: Float) {
		acceleration plusAssign gravity
		velocity plusAssign acceleration * interval
		val collider = parent.getComponent<Collider2DComponent>()
		val movement = velocity * interval
		if (collider != null && collider.active) {
			val collidedMove = collider.checkAllCollisions(movement, stepBias)
			parent.translate(Vec3(collidedMove, 0f))

			// If a collision is detected in either direction then set the velocity to 0
			if ((collidedMove.x < movement.x && velocity.x > 0) || (collidedMove.x > movement.x && velocity.x < 0)) velocity.x =
				0f
			if ((collidedMove.y < movement.y && velocity.y > 0) || (collidedMove.y > movement.y && velocity.y < 0)) velocity.y =
				0f
		} else parent.translate(Vec3(movement, 0f))

		acceleration = Vec2(0f)
	}
}