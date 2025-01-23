package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component

abstract class RenderedComponent(parent: GameObject) : Component(parent), RenderedComponentI {

	final override var visible = true

}