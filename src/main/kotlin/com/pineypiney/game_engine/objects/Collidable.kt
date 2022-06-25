package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.util.collision.CollisionBox2D

interface Collidable {
    val collider: CollisionBox2D
}