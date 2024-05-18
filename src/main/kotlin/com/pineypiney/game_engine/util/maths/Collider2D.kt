package com.pineypiney.game_engine.util.maths

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider2DComponent
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.maths.shapes.Rect2D

class Collider2D(override val shape: Rect2D) : Collider() {

	override fun getComponent(parent: GameObject): Component {
		return Collider2DComponent(parent, shape)
	}
}