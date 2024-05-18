package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.transforms.Transform3D
import com.pineypiney.game_engine.util.extension_functions.*
import glm_.quat.Quat
import glm_.vec3.Vec3

open class TransformComponent(parent: GameObject): Component(parent, "T2D") {

    val transform: Transform3D = Transform3D.origin

    var worldPosition: Vec3
        get() = if(parent.parent == null) transform.position else parent.worldModel.getTranslation()
        set(value) {
            val p = parent.parent?.worldModel
            if(p == null) parent.position = value
            else parent.position = (parent.worldModel.setTranslation(value) / p).getTranslation()
        }
    var worldRotation: Quat
        get() = if(parent.parent == null) transform.rotation else parent.worldModel.getRotation()
        set(value) {
            val p = parent.parent?.worldModel
            if(p == null) parent.rotation = value
            else parent.rotation = (parent.worldModel.setRotation(value) / p).getRotation()
        }
    var worldScale: Vec3
        get() = if(parent.parent == null) transform.scale else parent.worldModel.getScale()
        set(value) {
            val p = parent.parent?.worldModel
            if(p == null) parent.scale = value
            else parent.scale = (parent.worldModel.setScale(value) / p).getScale()
        }

    var velocity: Vec3 = Vec3()

    override val fields: Array<Field<*>> = arrayOf(
        Vec3Field("vlt", ::velocity){ velocity = it },
        Vec3Field("pos", transform::position){ transform.position = it },
        Vec3Field("scl", transform::scale){ transform.scale = it },
        QuatField("rtn", transform::rotation){ transform.rotation = it }
    )
}