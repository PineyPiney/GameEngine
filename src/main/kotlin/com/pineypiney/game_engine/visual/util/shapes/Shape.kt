package com.pineypiney.game_engine.visual.util.shapes

import com.pineypiney.game_engine.visual.Deleteable
import org.lwjgl.opengl.GL46C.*

open class Shape(private val vertices: FloatArray, val numVertices: Int): Deleteable {

    private val VAO = glGenVertexArrays()

    init {

        val VBO = glGenBuffers()
        glBindVertexArray(VAO)
        glBindBuffer(GL_ARRAY_BUFFER, VBO)

        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
        glEnableVertexAttribArray(2)

        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        // How to read non-indices array
        val bytes = 4 //Float.BYTES
        val stride = vertices.size/numVertices
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride * bytes, 0)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride * bytes, 3L * bytes)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride * bytes, 6L * bytes)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        glDeleteBuffers(VBO)
    }

    fun bind(){
        glBindVertexArray(this.VAO)
    }

    override fun delete() {
        glDeleteVertexArrays(this.VAO)
    }

    companion object {

        val cornerSquareVertices = floatArrayOf(
            // positions        // normals          // texture co-ords
            0.0,  0.0,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0,
            1.0,  0.0,  0.0,    0.0,  0.0,  1.0,    1.0, 0.0,
            1.0,  1.0,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  1.0,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            0.0,  1.0,  0.0,    0.0,  0.0,  1.0,    0.0, 1.0,
            0.0,  0.0,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0
        )
        val centerSquareVertices = floatArrayOf(
            // positions        // normals          // texture co-ords
            -0.5,  -0.5,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0,
            0.5,  -0.5,  0.0,    0.0,  0.0,  1.0,    1.0, 0.0,
            0.5,  0.5,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            0.5,  0.5,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            -0.5,  0.5,  0.0,    0.0,  0.0,  1.0,    0.0, 1.0,
            -0.5,  -0.5,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0
        )
        val screenQuadVertices = floatArrayOf(
            // positions        // normals          // texture co-ords
           -1.0, -1.0, 0.0,     0.0,  0.0,  1.0,    0.0, 0.0,
            1.0, -1.0, 0.0,     0.0,  0.0,  1.0,    1.0, 0.0,
            1.0,  1.0, 0.0,     0.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  1.0, 0.0,     0.0,  0.0,  1.0,    1.0, 1.0,
           -1.0,  1.0, 0.0,     0.0,  0.0,  1.0,    0.0, 1.0,
           -1.0, -1.0, 0.0,     0.0,  0.0,  1.0,    0.0, 0.0
        )

        val cornerSquareShape = Shape(cornerSquareVertices, 6)
        val centerSquareShape = Shape(centerSquareVertices, 6)
        val screenQuadShape = Shape(screenQuadVertices, 6)
    }
}

fun floatArrayOf(vararg elements: Double) : FloatArray {
    val ret = FloatArray(elements.size)

    for(i in elements.indices){
        ret[i] = elements.get(i).toFloat()
    }

    return ret
}