package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec2.Vec2

class DefaultObjects {

	companion object {

		fun barrier2D(name: String, bl: Vec2, size: Vec2, rotation: Float = 0f, render: Boolean = false): GameObject {
			return barrier2D(name, Rect2D(bl, size, rotation), render)
		}

		fun barrier2D(name: String, shape: Shape2D, render: Boolean = false): GameObject {
			val obj = GameObject(name)
			obj.components.add(Collider2DComponent(obj, shape))
			if (render) obj.addChild(CollisionBox2DRenderer.create(obj))
			return obj
		}
	}
}