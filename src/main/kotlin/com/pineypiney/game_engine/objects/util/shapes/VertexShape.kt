package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.objects.Deleteable
import glm_.f
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30C.*

abstract class VertexShape: Deleteable {

    val VAO = glGenVertexArrays()
    abstract val size: Int

    open fun bind(){
        glBindVertexArray(this.VAO)
    }

    abstract fun draw(mode: Int = GL_TRIANGLES)

    fun bindAndDraw(mode: Int = GL_TRIANGLES){
        bind()
        draw(mode)
    }

    abstract fun drawInstanced(amount: Int, mode: Int = GL_TRIANGLES)

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
        glDeleteVertexArrays(VAO)
    }

    companion object{

        fun getBuffer(buffer: Int, target: Int = GL_ARRAY_BUFFER): FloatArray{
            glBindBuffer(target, buffer)
            // Size is the buffer size in bytes, to must be divided by four to get the number of floats
            val size = glGetBufferParameteri(target, GL_BUFFER_SIZE)
            val a = FloatArray(size / 4)
            glGetBufferSubData(target, 0, a)
            return a
        }

        fun floatArrayOf(vararg elements: Number) : FloatArray {
            return elements.map { it.f }.toFloatArray()
        }


        val cornerSquareShape2D = SquareShape(Vec2(0.5), Vec2(1))
        val centerSquareShape2D = SquareShape(Vec2(), Vec2(1))
        val screenQuadShape = SquareShape(Vec2(), Vec2(2))
        val footSquare = SquareShape(Vec2(0, 0.5), Vec2(1))

        private val cornerCubeVertices = floatArrayOf(
            // positions        // normals          // texture co-ords
            // Back
            1.0,  0.0,  0.0,    0.0,  0.0,  -1.0,    1.0, 0.0,
            1.0,  1.0,  0.0,    0.0,  0.0,  -1.0,    1.0, 1.0,
            0.0,  1.0,  0.0,    0.0,  0.0,  -1.0,    0.0, 1.0,
            0.0,  1.0,  0.0,    0.0,  0.0,  -1.0,    0.0, 1.0,
            0.0,  0.0,  0.0,    0.0,  0.0,  -1.0,    0.0, 0.0,
            1.0,  0.0,  0.0,    0.0,  0.0,  -1.0,    1.0, 0.0,

            // Front
            0.0,  0.0,  1.0,    0.0,  0.0,  1.0,    0.0, 0.0,
            0.0,  1.0,  1.0,    0.0,  0.0,  1.0,    0.0, 1.0,
            1.0,  1.0,  1.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  1.0,  1.0,    0.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  0.0,  1.0,    0.0,  0.0,  1.0,    1.0, 0.0,
            0.0,  0.0,  1.0,    0.0,  0.0,  1.0,    0.0, 0.0,

            // Left
            0.0,  0.0,  0.0,    -1.0,  0.0,  0.0,    0.0, 0.0,
            0.0,  1.0,  0.0,    -1.0,  0.0,  0.0,    0.0, 1.0,
            0.0,  1.0,  1.0,    -1.0,  0.0,  0.0,    1.0, 1.0,
            0.0,  1.0,  1.0,    -1.0,  0.0,  0.0,    1.0, 1.0,
            0.0,  0.0,  1.0,    -1.0,  0.0,  0.0,    1.0, 0.0,
            0.0,  0.0,  0.0,    -1.0,  0.0,  0.0,    0.0, 0.0,

            // Right
            1.0,  0.0,  1.0,    1.0,  0.0,  1.0,    0.0, 0.0,
            1.0,  1.0,  1.0,    1.0,  0.0,  1.0,    0.0, 1.0,
            1.0,  1.0,  0.0,    1.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  1.0,  0.0,    1.0,  0.0,  1.0,    1.0, 1.0,
            1.0,  0.0,  0.0,    1.0,  0.0,  1.0,    1.0, 0.0,
            1.0,  0.0,  1.0,    1.0,  0.0,  1.0,    0.0, 0.0,

            // Bottom
            0.0,  0.0,  0.0,    0.0,  -1.0,  0.0,    0.0, 0.0,
            0.0,  0.0,  1.0,    0.0,  -1.0,  0.0,    0.0, 1.0,
            1.0,  0.0,  1.0,    0.0,  -1.0,  0.0,    1.0, 1.0,
            1.0,  0.0,  1.0,    0.0,  -1.0,  0.0,    1.0, 1.0,
            1.0,  0.0,  0.0,    0.0,  -1.0,  0.0,    1.0, 0.0,
            0.0,  0.0,  0.0,    0.0,  -1.0,  0.0,    0.0, 0.0,

            // Top
            0.0,  1.0,  1.0,    0.0,  1.0,  0.0,    0.0, 0.0,
            0.0,  1.0,  0.0,    0.0,  1.0,  0.0,    0.0, 1.0,
            1.0,  1.0,  0.0,    0.0,  1.0,  0.0,    1.0, 1.0,
            1.0,  1.0,  0.0,    0.0,  1.0,  0.0,    1.0, 1.0,
            1.0,  1.0,  1.0,    0.0,  1.0,  0.0,    1.0, 0.0,
            0.0,  1.0,  1.0,    0.0,  1.0,  0.0,    0.0, 0.0,
        )
        private val centerCubeVertices = floatArrayOf(
            // positions        // normals          // texture co-ords
            // Back
            0.5,  -0.5,  -0.5,    0.0,  0.0,  -1.0,    1.0, 0.0,
            0.5,  0.5,  -0.5,    0.0,  0.0,  -1.0,    1.0, 1.0,
            -0.5,  0.5,  -0.5,    0.0,  0.0,  -1.0,    0.0, 1.0,
            -0.5,  0.5,  -0.5,    0.0,  0.0,  -1.0,    0.0, 1.0,
            -0.5,  -0.5,  -0.5,    0.0,  0.0,  -1.0,    0.0, 0.0,
            0.5,  -0.5,  -0.5,    0.0,  0.0,  -1.0,    1.0, 0.0,

            // Front
            -0.5,  -0.5,  0.5,    0.0,  0.0,  1.0,    0.0, 0.0,
            -0.5,  0.5,  0.5,    0.0,  0.0,  1.0,    0.0, 1.0,
            0.5,  0.5,  0.5,    0.0,  0.0,  1.0,    1.0, 1.0,
            0.5,  0.5,  0.5,    0.0,  0.0,  1.0,    1.0, 1.0,
            0.5,  -0.5,  0.5,    0.0,  0.0,  1.0,    1.0, 0.0,
            -0.5,  -0.5,  0.5,    0.0,  0.0,  1.0,    0.0, 0.0,

            // Left
            -0.5,  -0.5,  -0.5,    -1.0,  0.0,  0.0,    0.0, 0.0,
            -0.5,  0.5,  -0.5,    -1.0,  0.0,  0.0,    0.0, 1.0,
            -0.5,  0.5,  0.5,    -1.0,  0.0,  0.0,    1.0, 1.0,
            -0.5,  0.5,  0.5,    -1.0,  0.0,  0.0,    1.0, 1.0,
            -0.5,  -0.5,  0.5,    -1.0,  0.0,  0.0,    1.0, 0.0,
            -0.5,  -0.5,  -0.5,    -1.0,  0.0,  0.0,    0.0, 0.0,

            // Right
            0.5,  -0.5,  0.5,    1.0,  0.0,  1.0,    0.0, 0.0,
            0.5,  0.5,  0.5,    1.0,  0.0,  1.0,    0.0, 1.0,
            0.5,  0.5,  -0.5,    1.0,  0.0,  1.0,    1.0, 1.0,
            0.5,  0.5,  -0.5,    1.0,  0.0,  1.0,    1.0, 1.0,
            0.5,  -0.5,  -0.5,    1.0,  0.0,  1.0,    1.0, 0.0,
            0.5,  -0.5,  0.5,    1.0,  0.0,  1.0,    0.0, 0.0,

            // Bottom
            -0.5,  -0.5,  -0.5,    0.0,  -1.0,  0.0,    0.0, 0.0,
            -0.5,  -0.5,  0.5,    0.0,  -1.0,  0.0,    0.0, 1.0,
            0.5,  -0.5,  0.5,    0.0,  -1.0,  0.0,    1.0, 1.0,
            0.5,  -0.5,  0.5,    0.0,  -1.0,  0.0,    1.0, 1.0,
            0.5,  -0.5,  -0.5,    0.0,  -1.0,  0.0,    1.0, 0.0,
            -0.5,  -0.5,  -0.5,    0.0,  -1.0,  0.0,    0.0, 0.0,

            // Top
            -0.5,  0.5,  0.5,    0.0,  1.0,  0.0,    0.0, 0.0,
            -0.5,  0.5,  -0.5,    0.0,  1.0,  0.0,    0.0, 1.0,
            0.5,  0.5,  -0.5,    0.0,  1.0,  0.0,    1.0, 1.0,
            0.5,  0.5,  -0.5,    0.0,  1.0,  0.0,    1.0, 1.0,
            0.5,  0.5,  0.5,    0.0,  1.0,  0.0,    1.0, 0.0,
            -0.5,  0.5,  0.5,    0.0,  1.0,  0.0,    0.0, 0.0,
        )

        val cornerCubeShape = ArrayShape(cornerCubeVertices, intArrayOf(3, 3, 2))
        val centerCubeShape = ArrayShape(centerCubeVertices, intArrayOf(3, 3, 2))
    }
}