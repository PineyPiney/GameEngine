package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.components.ComponentI
import glm_.vec2.Vec2

interface Collider2DCallback : ComponentI {
	fun onCollide(thisCollider: Collider2DComponent, otherCollider: Collider2DComponent, overlap: Vec2)
}