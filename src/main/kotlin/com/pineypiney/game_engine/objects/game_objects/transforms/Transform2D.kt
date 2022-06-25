package com.pineypiney.game_engine.objects.game_objects.transforms

import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Transform2D(position: Vec2 = Vec2(), rotation: Float = 0f, scale: Vec2 = Vec2(1)): Transform<Vec2, Float, Vec2>() {

    override var position: Vec2 = position
        set(value) {
            field = value
            recalculateModel()
        }
    override var rotation: Float = rotation
        set(value) {
            field = value
            recalculateModel()
        }
    override var scale: Vec2 = scale
        set(value) {
            field = value
            recalculateModel()
        }

    init {
        recalculateModel()
    }

    override fun translate(move: Vec2){
        position plusAssign move
        recalculateModel()
    }

    override fun rotate(angle: Float){
        rotation += angle
        recalculateModel()
    }

    override fun scale(mult: Vec2){
        scale timesAssign mult
        recalculateModel()
    }

    override fun recalculateModel(){
        model = I.translate(Vec3(position)).rotate(rotation, normal).scale(Vec3(scale, 1))
    }

    override fun copy(): Transform2D = Transform2D(position.copy(), rotation, scale.copy()).apply { recalculateModel() }

    companion object{
        val origin; get() = Transform2D()
    }
}