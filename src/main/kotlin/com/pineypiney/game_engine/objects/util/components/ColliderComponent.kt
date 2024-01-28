package com.pineypiney.game_engine.objects.util.components

import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2D
import com.pineypiney.game_engine.objects.util.collision.SoftCollisionBox
import glm_.vec2.Vec2

class ColliderComponent(parent: GameObject2D, val collider: CollisionBox2D): Component("C2D", parent) {

    constructor(parent: GameObject2D): this(parent, SoftCollisionBox(parent, Vec2(), Vec2(1)))

    override val fields: Array<Field<*>> = arrayOf(
        StorableField("prt", collider::parent) { p -> collider.parent = p },
        Vec2Field("ogn", collider::origin){ o -> collider.origin = o },
        Vec2Field("sze", collider::size){ s -> collider.size = s },
        FloatField("rtn", collider::rotation){ r -> collider.rotation = r},
        BooleanField("atv", collider::active){ a -> collider.active = a}
    )
}