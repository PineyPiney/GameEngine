package com.pineypiney.game_engine.objects.util

import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Transform(position: Vec2 = Vec2(), rotation: Float = 0f, scale: Vec2 = Vec2(1)): Copyable<Transform> {

    var model = I

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

    init {
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

    override fun equals(other: Any?): Boolean {
        if(other !is Transform) return false
        return this.position == other.position &&
                this.rotation == other.rotation &&
                this.scale == other.scale
    }

    override fun hashCode(): Int {
        return this.position.hashCode() + this.rotation.hashCode() + this.scale.hashCode()
    }

    override fun copy(): Transform = Transform(position.copy(), rotation, scale.copy()).apply { recalculateModel() }

    companion object{
        val origin; get() = Transform()
    }
}