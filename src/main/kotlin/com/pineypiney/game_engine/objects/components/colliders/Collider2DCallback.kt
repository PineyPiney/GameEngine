package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.components.ComponentI

interface Collider2DCallback : ComponentI {
	fun onCollide(thisCollider: Collider2DComponent, otherCollider: Collider2DComponent)
}