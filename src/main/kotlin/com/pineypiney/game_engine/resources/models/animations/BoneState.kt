package com.pineypiney.game_engine.resources.models.animations

import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.util.extension_functions.lerp
import com.pineypiney.game_engine.util.extension_functions.lerpAngle
import glm_.f
import glm_.vec2.Vec2

class BoneState(boneId: String, val translation: Vec2, val rotation: Float): State(boneId){

    override fun lerpWith(nextState: State, delta: Number): BoneState {
        return if(nextState is BoneState)
            BoneState(this.parentId, this.translation.lerp(nextState.translation, delta.f), this.rotation.lerpAngle(nextState.rotation, delta.f))
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

