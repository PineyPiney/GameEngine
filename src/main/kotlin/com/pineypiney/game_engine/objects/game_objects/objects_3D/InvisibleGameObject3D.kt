package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.copy
import glm_.vec3.Vec3

class InvisibleGameObject3D(position: Vec3 = Vec3(), scale: Vec3 = Vec3(1)) : GameObject3D() {

    override val id: ResourceKey = ResourceKey("barrier")

    init {
        this.position = position
        this.scale = scale
    }

    override fun copy(): InvisibleGameObject3D {
        return InvisibleGameObject3D(position.copy())
    }
}