package com.pineypiney.game_engine.visual.util.shapes

import glm_.vec2.Vec2

class TextQuad(val topLeft: Vec2 = Vec2(), val bottomRight: Vec2 = Vec2(1)) : Shape(createTextVertices(topLeft, bottomRight), 6){

    companion object{

        fun createTextVertices(topLeft: Vec2, bottomRight: Vec2) : FloatArray {
            return floatArrayOf(
                // Positions        Normal              Texture
                0.0f, 0.0f, 0.0f,   0.0f, 0.0f, 1.0f,   topLeft.x, bottomRight.y,
                1.0f, 0.0f, 0.0f,   0.0f, 0.0f, 1.0f,   bottomRight.x, bottomRight.y,
                1.0f, 1.0f, 0.0f,   0.0f, 0.0f, 1.0f,   bottomRight.x, topLeft.y,
                1.0f, 1.0f, 0.0f,   0.0f, 0.0f, 1.0f,   bottomRight.x, topLeft.y,
                0.0f, 1.0f, 0.0f,   0.0f, 0.0f, 1.0f,   topLeft.x, topLeft.y,
                0.0f, 0.0f, 0.0f,   0.0f, 0.0f, 1.0f,   topLeft.x, bottomRight.y
            )
        }
    }
}


