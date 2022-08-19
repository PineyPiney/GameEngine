package com.pineypiney.game_engine.resources.models

import glm_.vec2.Vec2

data class Geometry(val name: String, val vertices: Array<ModelLoader.VertexPosition>, val texMaps: Array<Vec2>, val indices: IntArray, val texture: String, val alpha: Float, val order: Int)