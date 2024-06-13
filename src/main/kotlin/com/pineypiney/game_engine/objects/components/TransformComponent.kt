package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.transforms.Transform3D
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3

open class TransformComponent(parent: GameObject): Component(parent, "T2D") {

    val transform: Transform3D = Transform3D.origin

    private var dirtyWorldModel = true
    private var worldModel: Mat4 = I
    
    var position: Vec3
        get() = transform.position
        set(value) { transform.position = value; dirtyWorldModel() }

    var rotation: Quat
        get() = transform.rotation
        set(value) { transform.rotation = value; dirtyWorldModel() }

    var scale: Vec3
        get() = transform.scale
        set(value) { transform.scale = value; dirtyWorldModel() }

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

    override val fields: Array<Field<*>> = arrayOf(
        Vec3Field("pos", transform::position){ transform.position = it },
        Vec3Field("scl", transform::scale){ transform.scale = it },
        QuatField("rtn", transform::rotation){ transform.rotation = it }
    )

    infix fun translate(move: Vec3) {
        transform translate move
        dirtyWorldModel()
    }
    infix fun rotate(angle: Quat) {
        transform rotate angle
        dirtyWorldModel()
    }
    infix fun rotate(euler: Vec3) {
        transform rotate euler
        dirtyWorldModel()
    }
    infix fun scale(mult: Vec3) {
        transform scale mult
        dirtyWorldModel()
    }
    
    fun fetchModel() = transform.fetchModel()

    fun fetchWorldModel(): Mat4{
        if(dirtyWorldModel) {
            worldModel = parent.parent?.let { it.worldModel * transform.fetchModel() } ?: transform.fetchModel()
            dirtyWorldModel = false
        }
        return worldModel
    }
    
    private fun dirtyWorldModel(){
        for(d in parent.allDescendants()) d.transformComponent.dirtyWorldModel = true
    }
}