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
    }
}