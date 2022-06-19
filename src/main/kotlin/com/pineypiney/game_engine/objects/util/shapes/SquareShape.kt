package com.pineypiney.game_engine.objects.util.shapes

import org.lwjgl.opengl.GL11C.GL_TRIANGLE_FAN

class SquareShape(vertices: FloatArray, indices: IntArray = intArrayOf(2, 2)): ArrayShape(vertices, indices) {

    override fun draw(mode: Int) {
        super.draw(GL_TRIANGLE_FAN)
    }
}