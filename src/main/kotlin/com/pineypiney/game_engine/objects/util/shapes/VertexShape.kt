package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.associateIndexed
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.f
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL30C.*

abstract class VertexShape: Deleteable {

    val VAO = if(GLFunc.isLoaded) glGenVertexArrays() else -1
    abstract val size: Int

    abstract val shape: Shape

    open fun bind(){
        glBindVertexArray(this.VAO)
    }

    abstract fun draw(mode: Int = GL_TRIANGLES)

    fun bindAndDraw(mode: Int = GL_TRIANGLES){
        bind()
        draw(mode)
    }

    abstract fun drawInstanced(amount: Int, mode: Int = GL_TRIANGLES)

    fun setAttribs(data: Map<Int, Pair<Int, Int>>){

        // How to read non-indices array
        val stride = data.values.sumOf { 4 * it.second }

        var step = 0L
        for((index, d) in data){
            val (type, size) = d


            glEnableVertexAttribArray(index)
            when(type){
                GL_FLOAT -> glVertexAttribPointer(index, size, type, false, stride, step)
                GL_INT -> glVertexAttribIPointer(index, size, type, stride, step)
            }

            step += size * 4
        }
    }

    fun setAttribs(parts: IntArray, type: Int = GL_FLOAT){
        setAttribs(parts.toList().associateIndexed { i, p -> Pair(i, Pair(type, p)) })
    }

    abstract fun getVertices(): FloatArray

    override fun delete() {
        glDeleteVertexArrays(VAO)
    }

    companion object{

        fun getFloatBuffer(buffer: Int, target: Int): FloatArray{
            glBindBuffer(target, buffer)
            // Size is the buffer size in bytes, to must be divided by four to get the number of floats
            val size = glGetBufferParameteri(target, GL_BUFFER_SIZE)
            val a = FloatArray(size / 4)
            glGetBufferSubData(target, 0, a)
            return a
        }

        fun getIntBuffer(buffer: Int, target: Int): IntArray{
            glBindBuffer(target, buffer)
            // Size is the buffer size in bytes, to must be divided by four to get the number of floats
            val size = glGetBufferParameteri(target, GL_BUFFER_SIZE)
            val a = IntArray(size / 4)
            glGetBufferSubData(target, 0, a)
            return a
        }

        fun floatArrayOf(vararg elements: Number) : FloatArray {
            return elements.map { it.f }.toFloatArray()
        }


        val cornerSquareShape = SquareShape(Vec2(), Vec2(1f))
        val centerSquareShape = SquareShape(Vec2(-0.5f), Vec2(0.5f))
        val screenQuadShape = SquareShape(Vec2(-1f), Vec2(1f))
        val footSquare = SquareShape(Vec2(-0.5f, 0f), Vec2(0.5f, 1f))

        val cornerCubeShape = CubeShape(Vec3(0f), Vec3(1f))
        val centerCubeShape = CubeShape(Vec3(-.5f), Vec3(.5f))
    }
}