package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec3.Vec3

class ModelMaterial(val name: String, val textures: Map<String, Texture>, val baseColour: Vec3 = Vec3(1)) {
}