package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3t
import glm_.vec3.swizzle.xy

abstract class GameObject2D : OldGameObject() {

    open var velocity2D: Vec2
        get() = Vec2( transformComponent.velocity)
        set(value) { transformComponent.velocity.xy = value }

    open var position2D: Vec2
        get() = Vec2(transform.position)
        set(value){
            transform.position = Vec3(value, transform.position.z)
        }
    open var scale2D: Vec2
        get() = Vec2(transform.scale)
        set(value){
            transform.scale = Vec3(value, transform.scale.z)
        }
    open var rotation2D: Float
        get() = transform.rotation.w
        set(value){
            transform.rotation.w = value
        }

    // Items are rendered in order of depth, from inf to -inf
    open var depth: Int
        get() = transformComponent.depth
        set(value) { transformComponent.depth = value }

    fun setPosition(pos: Vec3t<*>){
        position2D = Vec2(pos)
        depth = pos.z.i
    }

    fun translate(move: Vec2){
        transform translate Vec3(move, 0f)
    }

    fun rotate(angle: Float){
        transform.rotate(Vec3(0, 0, angle))
    }

    fun scale(mult: Vec2){
        transform.scale(mult)
    }

    open fun isCovered(point: Vec2): Boolean{
        val originTranslation = Vec3(point - this.position2D)
        val transform = I.rotate(-this.rotation2D, normal).translate(originTranslation)
        val vec = Vec2(transform[3][0], transform[3][1])

        return vec.isWithin(Vec2(-(0.5f * scale2D.x), 0), scale2D)
    }
}