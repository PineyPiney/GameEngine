package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.copy
import glm_.vec2.Vec2

class InvisibleGameObject(position: Vec2 = Vec2(), scale: Vec2 = Vec2(1)) : GameObject() {

    override val id: ResourceKey = ResourceKey("barrier")

    init {
        this.position = position
        this.scale = scale
    }

    override fun copy(): InvisibleGameObject {
        return InvisibleGameObject(position.copy())
    }
}