package com.pineypiney.game_engine.objects.util

import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.normal
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Transform(position: Vec2, rotation: Float, scale: Vec2): Copyable<Transform> {

    var model = I
    val c; get() = copy()

    var position: Vec2 = position
        set(value) {
            field = value
            recalculateModel()
        }
    var rotation: Float = rotation
        set(value) {
            field = value
            recalculateModel()
        }
    var scale: Vec2 = scale
        set(value) {
            field = value
            recalculateModel()
        }

    fun translate(move: Vec2){
        position plusAssign move
        recalculateModel()
    }

    fun rotate(angle: Float){
        rotation += angle
        recalculateModel()
    }

    fun scale(mult: Vec2){
        scale timesAssign mult
        recalculateModel()
    }

    private fun recalculateModel(){
        model = I.translate(Vec3(position)).rotate(rotation, normal).scale(Vec3(scale, 1))
    }

    override fun copy(): Transform = Transform(position.copy(), rotation, scale.copy())

    companion object{
        val origin = Transform(Vec2(), 0f, Vec2(1))
    }
}