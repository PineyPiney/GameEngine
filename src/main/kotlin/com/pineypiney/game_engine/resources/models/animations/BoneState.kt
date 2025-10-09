package com.pineypiney.game_engine.resources.models.animations

import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.util.extension_functions.lerp
import glm_.f
import glm_.quat.Quat
import glm_.vec3.Vec3

class BoneState(boneId: String, var translation: Vec3, var rotation: Quat) : State(boneId) {

	override fun lerpWith(nextState: State, delta: Number): BoneState {
		return if (nextState is BoneState)
			BoneState(
				this.parentId,
				this.translation.lerp(nextState.translation, delta.f),
				this.rotation.slerp(nextState.rotation, delta.f)
			)
		else this
	}

	override fun applyTo(model: Model) {
		val bone = model.findBone(this.parentId) ?: return
		bone.translation = this.translation
		bone.rotation = this.rotation
	}

	override fun toString(): String {
		return "BoneState($parentId)"
	}
}

