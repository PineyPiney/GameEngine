package com.pineypiney.game_engine.objects.util.shapes

import org.lwjgl.opengl.GL46C.*

open class ArrayShape(vertices: FloatArray, parts: IntArray): Shape() {

    override val size = vertices.size / parts.sum()

    init {

        val VBO = glGenBuffers()
        glBindVertexArray(VAO)

        glBindBuffer(GL_ARRAY_BUFFER, VBO)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        // How to read non-indices array
        setAttribs(parts)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        glDeleteBuffers(VBO)
    }

    override fun draw(mode: Int){
        glDrawArrays(mode, 0, size)
    }

    override fun drawInstanced(amount: Int, mode: Int) {
        glDrawArraysInstanced(mode, 0, size, amount)
    }
}