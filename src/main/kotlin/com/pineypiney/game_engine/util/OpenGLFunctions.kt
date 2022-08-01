package com.pineypiney.game_engine.util

import glm_.bool
import kool.ByteBuffer
import kool.lib.toByteArray
import org.lwjgl.opengl.GL11.*

class OpenGLFunctions {

    companion object{
        fun getFloats(pname: Int, size: Int): FloatArray{
            val array = FloatArray(size)
            glGetFloatv(pname, array)

            return array
        }

        fun getDoubles(pname: Int, size: Int): DoubleArray{
            val array = DoubleArray(size)
            glGetDoublev(pname, array)

            return array
        }

        fun getInts(pname: Int, size: Int): IntArray{
            val array = IntArray(size)
            glGetIntegerv(pname, array)

            return array
        }

        fun getBools(pname: Int, size: Int): BooleanArray{
            val buffer = ByteBuffer(size)
            glGetBooleanv(pname, buffer)

            return buffer.toByteArray().map { it.bool }.toBooleanArray()
        }
    }
}