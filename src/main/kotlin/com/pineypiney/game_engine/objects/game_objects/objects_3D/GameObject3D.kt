package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform3D
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class GameObject3D : GameObject() {

    override val transform: Transform3D = Transform3D.origin

    open var velocity: Vec2 = Vec2()

    open var position: Vec3
        get() = transform.position
        set(value){
            transform.position = value
        }
    open var rotation: Quat
        get() = transform.rotation
        set(value){
            transform.rotation = value
        }
    open var scale: Vec3
        get() = transform.scale
        set(value){
            transform.scale = value
        }

    override fun init() {}

    fun getWidth(): Float{
        return scale.x
    }

    fun translate(move: Vec3){
        transform.translate(move)
    }

    fun rotate(quat: Quat){
        transform.rotate(quat)
    }

    fun rotate(euler: Vec3){
        transform.rotate(euler)
    }

    fun scale(mult: Vec3){
        transform.scale(mult)
    }
}