package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import glm_.quat.Quat
import glm_.vec3.Vec3

class BoxCollider3DComponent(parent: GameObject, override val shape: Cuboid) : Collider3DComponent(parent, shape) {

	constructor(parent: GameObject) : this(parent, Cuboid(Vec3(), Quat.identity, Vec3(1)))

}