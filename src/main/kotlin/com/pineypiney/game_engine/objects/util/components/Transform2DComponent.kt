package com.pineypiney.game_engine.objects.util.components

import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D
import glm_.vec2.Vec2

class Transform2DComponent(parent: Storable, val transform: Transform2D): Component("T2D", parent) {

    constructor(parent: Storable): this(parent, Transform2D.origin)

    var velocity: Vec2 = Vec2()
    var depth: Int = 0

    override val fields: Array<Field<*>> = arrayOf(
        Vec2Field("vlt", ::velocity){ velocity = it },
        Vec2Field("pos", transform::position){ transform.position = it },
        Vec2Field("scl", transform::scale){ transform.scale = it },
        FloatField("rtn", transform::rotation){ transform.rotation = it },
        IntField("dpt", ::depth){ depth = it },
    )
}