package com.pineypiney.game_engine.visual.util.collision

import com.pineypiney.game_engine.visual.game_objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.copy
import glm_.vec2.Vec2

class SoftCollisionBox(parent: GameObject?, origin: Vec2, size: Vec2): CollisionBox(parent, origin, size) {

    override fun copy(): CollisionBox {
        val new = SoftCollisionBox(parent, origin.copy(), size.copy())
        new.active = this.active
        return new
    }
}