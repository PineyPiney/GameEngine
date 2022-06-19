package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.objects.Deleteable
import glm_.f
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL46C.*

abstract class Shape: Deleteable {
    val VAO = glGenVertexArrays()
    abstract val size: Int

    open fun bind(){
        glBindVertexArray(this.VAO)
    }

    abstract fun draw(mode: Int = GL_TRIANGLES)
    abstract fun drawInstanced(amount: Int, mode: Int = GL_TRIANGLES, )

    fun setAttribs(data: Array<Vec2i>, type: Int){

        // How to read non-indices array
        val bytes = 4 //Float.BYTES
        val stride = data.sumOf { it.y } * bytes

        var step = 0L
        for(attrib in data){
            val index = attrib.x
            val size = attrib.y

            glEnableVertexAttribArray(index)
            when(type){
                GL_FLOAT -> glVertexAttribPointer(index, size, type, false, stride, step)
                GL_INT -> glVertexAttribIPointer(index, size, type, stride, step)
            }

            step += size * bytes
        }
    }

    fun setAttribs(parts: IntArray){
        setAttribs(parts.mapIndexed{ i, p -> Vec2i(i, p) }.toTypedArray(), GL_FLOAT)
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

        val footSquareVertices = floatArrayOf(
            -0.5, 0, 0, 0,
            -0.5, 1, 0, 1,
            0.5, 1, 1, 1,
            0.5, 0, 1, 0
        )


        val cornerSquareShape2D = SquareShape(cornerSquareVertices)
        val centerSquareShape2D = SquareShape(centerSquareVertices)
        val screenQuadShape = SquareShape(screenQuadVertices)
        val footSquare = SquareShape(footSquareVertices)
    }
}