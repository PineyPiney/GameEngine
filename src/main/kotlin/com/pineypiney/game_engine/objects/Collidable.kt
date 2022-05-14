package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.util.collision.CollisionBox

interface Collidable {
    val collider: CollisionBox
}