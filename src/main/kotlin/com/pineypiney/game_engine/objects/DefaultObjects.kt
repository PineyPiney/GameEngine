package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2

class DefaultObjects {

	companion object {

		fun barrier2D(name: String, bl: Vec2, size: Vec2, rotation: Float = 0f, render: Boolean = false): GameObject {
			return object : GameObject(name) {
				override fun addComponents() {
					super.addComponents()
					components.add(Collider2DComponent(this, Rect2D(bl, size, rotation)))
				}

				override fun addChildren() {
					super.addChildren()
					if(render) addChild(CollisionBox2DRenderer.create(this))
				}
			}
		}
	}
}