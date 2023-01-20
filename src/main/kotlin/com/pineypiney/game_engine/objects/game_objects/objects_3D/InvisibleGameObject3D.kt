package com.pineypiney.game_engine.objects.game_objects.objects_3D

import glm_.vec3.Vec3

class InvisibleGameObject3D(position: Vec3 = Vec3(), scale: Vec3 = Vec3(1)) : GameObject3D() {

    init {
        this.position = position
        this.scale = scale
    }
}