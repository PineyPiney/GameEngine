package com.pineypiney.game_engine.objects.util.shapes

import glm_.vec2.Vec2
import org.lwjgl.opengl.GL11C.GL_TRIANGLE_FAN

class TextQuad(val topLeft: Vec2 = Vec2(), val bottomRight: Vec2 = Vec2(1)) : ArrayShape(createTextVertices(topLeft, bottomRight), intArrayOf(2, 2)){

    override fun draw(mode: Int) {
        super.draw(GL_TRIANGLE_FAN)
    }

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
