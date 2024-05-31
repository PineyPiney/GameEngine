package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.GameObject
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class MenuItem(name: String = "MenuItem") : GameObject(name) {

    override fun init() {
        super.init()
        layer = 1
    }

    fun os(origin: Vec2, size: Vec2){
        position = Vec3(origin, 0f)
        scale = Vec3(size, 1f)
    }

    fun os(origin: Vec3, size: Vec2){
        position = origin
        scale = Vec3(size, 1f)
    }
}