package com.pineypiney.game_engine.objects.util.shapes

import glm_.vec2.Vec2

class TextQuad(val topLeft: Vec2 = Vec2(), val bottomRight: Vec2 = Vec2(1)) : IndicesShape(createTextVertices(topLeft, bottomRight), intArrayOf(2, 2), intArrayOf(0, 1, 2, 2, 3, 0)){
    companion object{
        fun createTextVertices(topLeft: Vec2, bottomRight: Vec2) : FloatArray {
            return floatArrayOf(
                // Positions    Texture
                0.0f, 0.0f,     topLeft.x, bottomRight.y,
                1.0f, 0.0f,     bottomRight.x, bottomRight.y,
                1.0f, 1.0f,     bottomRight.x, topLeft.y,
                0.0f, 1.0f,     topLeft.x, topLeft.y,
            )
        }
    }
}
