package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.maths.shapes.CompoundShape3D

class CompoundCollider3DComponent(parent: GameObject, override val shape: CompoundShape3D): Collider3DComponent(parent) {


}