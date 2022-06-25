package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.util.extension_functions.copy
import glm_.vec2.Vec2

class SoftCollisionBox(parent: GameObject2D?, origin: Vec2, size: Vec2): CollisionBox2D(parent, origin, size) {

    override fun copy(): CollisionBox2D {
        val new = SoftCollisionBox(parent, origin.copy(), size.copy())
        new.active = this.active
        return new
    }
}