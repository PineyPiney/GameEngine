package com.pineypiney.game_engine.util

import org.lwjgl.opengl.GL11.*

class GLFunc {

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
            val array = IntArray(size)
            glGetIntegerv(pname, array)

            return array.map { it != 0 }.toBooleanArray()
        }
    }
}