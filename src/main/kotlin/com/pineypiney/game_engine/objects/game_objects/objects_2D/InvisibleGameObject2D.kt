package com.pineypiney.game_engine.objects.game_objects.objects_2D

import glm_.vec2.Vec2

class InvisibleGameObject2D(position: Vec2 = Vec2(), scale: Vec2 = Vec2(1)) : GameObject2D() {

    init {
        this.position = position
        this.scale = scale
    }
}