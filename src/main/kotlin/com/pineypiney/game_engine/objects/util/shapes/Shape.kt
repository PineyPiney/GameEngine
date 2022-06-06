package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.objects.Deleteable
import glm_.f
import org.lwjgl.opengl.GL46C.*

abstract class Shape: Deleteable {
    val VAO = glGenVertexArrays()
    abstract val size: Int

    open fun bind(){
        glBindVertexArray(this.VAO)
    }

    abstract fun draw(mode: Int = GL_TRIANGLES)

    fun setAttribs(parts: IntArray){

        // How to read non-indices array
        val bytes = 4 //Float.BYTES
        val stride = parts.sum()

        var step = 0L
        for(i in parts.indices){
            glEnableVertexAttribArray(i)

            val part = parts[i]
            glVertexAttribPointer(i, part, GL_FLOAT, false, stride * bytes, step * stride)
            step += part
        }
    }

    override fun delete() {
        glDeleteVertexArrays(this.VAO)
    }

    companion object{

        fun floatArrayOf(vararg elements: Number) : FloatArray {
            return elements.map { it.f }.toFloatArray()
        }


        private val cornerSquareVertices3 = floatArrayOf(
            // positions        // normals          // texture co-ords
            0.0,  0.0,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0,
            1.0,  0.0,  0.0,    0.0,  0.0,  1.0,    1.0, 0.0,
            1.0,  1.0,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  1.0,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            0.0,  1.0,  0.0,    0.0,  0.0,  1.0,    0.0, 1.0,
            0.0,  0.0,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0
        )
        private val centerSquareVertices3 = floatArrayOf(
            // positions        // normals          // texture co-ords
            -0.5,  -0.5,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0,
            0.5,  -0.5,  0.0,    0.0,  0.0,  1.0,    1.0, 0.0,
            0.5,  0.5,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            0.5,  0.5,  0.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            -0.5,  0.5,  0.0,    0.0,  0.0,  1.0,    0.0, 1.0,
            -0.5,  -0.5,  0.0,    0.0,  0.0,  1.0,    0.0, 0.0
        )

        val cornerSquareShape3D = ArrayShape(cornerSquareVertices3, intArrayOf(3, 3, 2))
        val centerSquareShape3D = ArrayShape(centerSquareVertices3, intArrayOf(3, 3, 2))


        private val cornerSquareVertices = floatArrayOf(
            // positions    // texture co-ords
            0, 0,           0, 0,
            1, 0,           1, 0,
            1, 1,           1, 1,
            0, 1,           0, 1,
        )
        private val centerSquareVertices = floatArrayOf(
            // positions    // texture co-ords
            -0.5, -0.5,           0, 0,
            0.5, -0.5,           1, 0,
            0.5, 0.5,           1, 1,
            -0.5, 0.5,           0, 1,
        )
        private val screenQuadVertices = floatArrayOf(
            // positions    // texture co-ords
            -1, -1,     0, 0,
            1, -1,      1, 0,
            1,  1,      1, 1,
            -1,  1,     0, 1,
        )

        val cornerSquareShape2D = IndicesShape(cornerSquareVertices, intArrayOf(2, 2), intArrayOf(0, 1, 2, 2, 3, 0))
        val centerSquareShape2D = IndicesShape(centerSquareVertices, intArrayOf(2, 2), intArrayOf(0, 1, 2, 2, 3, 0))
        val screenQuadShape = IndicesShape(screenQuadVertices, intArrayOf(2, 2), intArrayOf(0, 1, 2, 2, 3, 0))
    }
}