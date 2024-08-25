package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component

abstract class RenderedComponent(parent: GameObject) : Component(parent, "RND"), RenderedComponentI {

	final override var visible = true

	override val fields: Array<Field<*>> = arrayOf(
		BooleanField("vsb", ::visible) { visible = it }
	)
}