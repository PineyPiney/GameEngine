package com.pineypiney.game_engine.objects.game_objects.transforms

import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.I
import glm_.vec3.Vec3

class Transform3D(position: Vec3 = Vec3(), rotation: Vec3 = Vec3(), scale: Vec3 = Vec3(1)): Transform<Vec3, Vec3, Vec3>() {

    override var position: Vec3 = position
        set(value) {
            field = value
            recalculateModel()
        }
    override var rotation: Vec3 = rotation
        set(value) {
            field = value
            recalculateModel()
        }
    override var scale: Vec3 = scale
        set(value) {
            field = value
            recalculateModel()
        }

    init {
        recalculateModel()
    }

    override fun translate(move: Vec3){
        position plusAssign move
        recalculateModel()
    }

    override fun rotate(angle: Vec3){
        rotation plusAssign angle
        recalculateModel()
    }

    override fun scale(mult: Vec3){
        scale timesAssign mult
        recalculateModel()
    }

    override fun recalculateModel(){
        model = I.translate(position)
            .rotate(rotation.x, Vec3(1, 0, 0))
            .rotate(rotation.y, Vec3(0, 1, 0))
            .rotate(rotation.z, Vec3(0, 0, 1))
            .scale(scale)
    }

    override fun copy(): Transform3D = Transform3D(position.copy(), rotation.copy(), scale.copy()).apply { recalculateModel() }

    companion object{
        val origin; get() = Transform3D()
    }
}