package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.copy
import glm_.vec2.Vec2

class InvisibleGameObject2D(position: Vec2 = Vec2(), scale: Vec2 = Vec2(1)) : GameObject2D() {

    override val id: ResourceKey = ResourceKey("barrier")

    init {
        this.position = position
        this.scale = scale
    }

    override fun copy(): InvisibleGameObject2D {
        return InvisibleGameObject2D(position.copy())
    }
}