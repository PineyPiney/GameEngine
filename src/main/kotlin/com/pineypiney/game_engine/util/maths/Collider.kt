package com.pineypiney.game_engine.util.maths

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.maths.shapes.Shape

abstract class Collider {
	abstract val shape: Shape<*>

	abstract fun getComponent(parent: GameObject): Component
}