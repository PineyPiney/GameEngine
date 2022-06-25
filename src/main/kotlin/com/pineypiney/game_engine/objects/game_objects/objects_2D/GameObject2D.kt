package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class GameObject2D : GameObject() {

    override val objects: MutableSet<ObjectCollection> = mutableSetOf()

    override val transform: Transform2D = Transform2D.origin

    open var velocity: Vec2 = Vec2()

    open var position: Vec2
        get() = transform.position
        set(value){
            transform.position = value
        }
    open var scale: Vec2
        get() = transform.scale
        set(value){
            transform.scale = value
        }
    open var rotation: Float
        get() = transform.rotation
        set(value){
            transform.rotation = value
        }

    // Items are rendered in order of depth, from inf to -inf
    open var depth: Int = 0

    override fun init() {}

    fun getWidth(): Float{
        return scale.x
    }

    fun setPosition(pos: Vec3){
        position = Vec2(pos)
        depth = pos.z.i
    }

    fun translate(move: Vec2){
        transform.translate(move)
    }

    fun rotate(angle: Float){
        transform.rotate(angle)
    }

    fun scale(mult: Vec2){
        transform.scale(mult)
    }

    open fun isCovered(point: Vec2): Boolean{
        val originTranslation = Vec3(point - this.position)
        val transform = I.rotate(-this.rotation, normal).translate(originTranslation)
        val vec = Vec2(transform[3][0], transform[3][1])

        return vec.isWithin(Vec2(-(0.5f * scale.x), 0), scale)
    }
}