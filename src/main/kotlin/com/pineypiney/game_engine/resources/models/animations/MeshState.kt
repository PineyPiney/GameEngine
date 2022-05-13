package com.pineypiney.game_engine.resources.models.animations

import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.util.extension_functions.lerp
import com.pineypiney.game_engine.util.extension_functions.lerpAngle
import glm_.f
import glm_.vec2.Vec2

class MeshState(meshId: String, val translation: Vec2, val rotation: Float, val alpha: Float, val order: Int): State(meshId){

    override fun lerpWith(nextState: State, delta: Number): MeshState {
        return if(nextState is MeshState)
            MeshState(this.parentId, this.translation.lerp(nextState.translation, delta.f), this.rotation.lerpAngle(nextState.rotation, delta.f), this.alpha.lerp(nextState.alpha, delta.f), this.order)
            else this
    }

    override fun applyTo(model: Model) {
        val mesh = model.meshes.firstOrNull{it.id == this.parentId} ?: return
        mesh.translation = this.translation
        mesh.rotation = this.rotation
        mesh.alpha = this.alpha
        mesh.order = this.order
    }

    override fun toString(): String {
        return "MeshState($parentId)"
    }
}