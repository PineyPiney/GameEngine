package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.transforms.Transform3D
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import glm_.quat.Quat
import glm_.vec3.Vec3

class TransformComponent(parent: GameObject): Component("T2D", parent) {

    val transform: Transform3D = Transform3D.origin

    val worldPosition: Vec3 get() = if(parent.parent == null) transform.position else parent.worldModel.getTranslation()
    val worldRotation: Quat get() = if(parent.parent == null) transform.rotation else parent.worldModel.getRotation()
    val worldScale: Vec3 get() = if(parent.parent == null) transform.scale else parent.worldModel.getScale()

    var velocity: Vec3 = Vec3()
    var depth: Int = 0

    override val fields: Array<Field<*>> = arrayOf(
        Vec3Field("vlt", ::velocity){ velocity = it },
        Vec3Field("pos", transform::position){ transform.position = it },
        Vec3Field("scl", transform::scale){ transform.scale = it },
        QuatField("rtn", transform::rotation){ transform.rotation = it },
        IntField("dpt", ::depth){ depth = it },
    )
}