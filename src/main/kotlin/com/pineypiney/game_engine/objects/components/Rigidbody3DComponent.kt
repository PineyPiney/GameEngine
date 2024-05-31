package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import glm_.vec3.Vec3

class Rigidbody3DComponent(parent: GameObject) : Component(parent, "RGB"), UpdatingComponent {

	var velocity = Vec3()
	var acceleration = Vec3()

	var gravity = Vec3(0f, -9.81f, 0f)

	var stepBias: Vec3? = null

	override val fields: Array<Field<*>> = arrayOf(
		Vec3Field("vlc", ::velocity) { velocity = it },
		Vec3Field("acc", ::acceleration) { acceleration = it },
		Vec3Field("grv", ::gravity) { gravity = it },
		Vec3Field("spb", { stepBias ?: Vec3() }) { stepBias = it },
	)

	override fun update(interval: Float) {
		acceleration plusAssign  gravity
		velocity plusAssign acceleration * interval
		val collider = parent.getComponent<Collider3DComponent>()
		val movement = velocity * interval
		if(collider != null){
			val collidedMove = collider.checkAllCollisions(movement, stepBias)
			parent.translate(collidedMove)

			// If a collision is detected in either direction then set the velocity to 0
			if((collidedMove.x < movement.x && velocity.x > 0) || (collidedMove.x > movement.x && velocity.x < 0)) velocity.x = 0f
			if((collidedMove.y < movement.y && velocity.y > 0) || (collidedMove.y > movement.y && velocity.y < 0)) velocity.y = 0f
			if((collidedMove.z < movement.z && velocity.z > 0) || (collidedMove.z > movement.z && velocity.z < 0)) velocity.z = 0f
		}
		else parent.translate(movement)

		acceleration = Vec3()
	}
}