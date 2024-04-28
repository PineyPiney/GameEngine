package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import glm_.vec3.Vec3

open class RelativeTransformComponent(parent: GameObject, relPos: Vec3, origin: Vec3 = Vec3()): TransformComponent(parent), UpdatingAspectRatioComponent {

    var relPos: Vec3 = relPos
        set(value) {
            field = value
            recalculatePosition()
        }
    var origin: Vec3 = origin
        set(value) {
            field = value
            recalculatePosition()
        }

    init {
    	recalculatePosition()
    }

    fun recalculatePosition(){
        transform.position = relPos + (origin * transform.scale)
    }

    override fun updateAspectRatio(renderer: RendererI<*>) {
        recalculatePosition()
    }
}