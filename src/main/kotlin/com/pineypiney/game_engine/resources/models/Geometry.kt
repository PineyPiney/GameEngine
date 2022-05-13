package com.pineypiney.game_engine.resources.models

import glm_.vec2.Vec2
import glm_.vec3.Vec3

data class Geometry(val name: String, val vertices: Array<ModelLoader.VertexPosition>, val normals: Array<Vec3>, val texMaps: Array<Vec2>, val texture: String, val alpha: Float, val order: Int)