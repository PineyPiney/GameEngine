package com.pineypiney.game_engine.util.maths

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.util.maths.shapes.Cuboid

class Collider3D(override val shape: Cuboid) : Collider() {

	override fun getComponent(parent: GameObject): Component {
		return BoxCollider3DComponent(parent, shape)
	}
}