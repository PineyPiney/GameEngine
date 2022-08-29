package com.pineypiney.game_engine.objects.util.shapes

import glm_.vec2.Vec2
import org.lwjgl.opengl.GL11C.GL_TRIANGLE_FAN

class SquareShape(center: Vec2, size: Vec2): ArrayShape(createVertices(center, size / 2), intArrayOf(2, 2)) {

    override fun draw(mode: Int) {
        super.draw(GL_TRIANGLE_FAN)
    }

    companion object{
        fun createVertices(c: Vec2, b: Vec2): FloatArray{
            val bl = c - b
            val tr = c + b
            return floatArrayOf(
                bl.x, bl.y, 0, 0,
                bl.x, tr.y, 0, 1,
                tr.x, tr.y, 1, 1,
                tr.x, bl.y, 1, 0
            )
        }
    }
}