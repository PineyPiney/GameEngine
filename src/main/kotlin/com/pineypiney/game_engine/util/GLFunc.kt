package com.pineypiney.game_engine.util

import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import org.lwjgl.opengl.ARBImaging.GL_BLEND_COLOR
import org.lwjgl.opengl.GL30C.*

class GLFunc {

    companion object{

        var activeTexture: Int
            get() = glGetInteger(GL_ACTIVE_TEXTURE)
            set(value) = glActiveTexture(value)
        var blend: Boolean
            get() = glGetBoolean(GL_BLEND)
            set(value) = setBool(GL_BLEND, value)
        val blendFuncSrcRGB: Int get() = glGetInteger(GL_BLEND_SRC_RGB)
        val blendFuncSrcA: Int get() = glGetInteger(GL_BLEND_SRC_ALPHA)
        val blendFuncDstRGB: Int get() = glGetInteger(GL_BLEND_DST_RGB)
        val blendFuncDstA: Int get() = glGetInteger(GL_BLEND_DST_ALPHA)
        var blendFuncSeparate: Vec4i
            get() = Vec4i(blendFuncSrcRGB, blendFuncSrcA, blendFuncDstRGB, blendFuncDstA)
            set(value) = glBlendFuncSeparate(value.x, value.y, value.z, value.w)
        var blendFunc: Vec2i
            get() = Vec2i(blendFuncSrcRGB, blendFuncDstRGB)
            set(value) = glBlendFunc(value.x, value.y)
        var blendColour: Vec4
            get() = Vec4(0, getFloats(GL_BLEND_COLOR, 4))
            set(value) = glBlendColor(value.x, value.y, value.z, value.w)
        var clearColour: Vec4
            get() = Vec4(0, getFloats(GL_COLOR_CLEAR_VALUE, 4))
            set(value) = glClearColor(value.x, value.y, value.z, value.w)
        var cullface: Boolean
            get() = glGetBoolean(GL_CULL_FACE)
            set(value) = setBool(GL_CULL_FACE, value)
        var cullFaceMode
            get() = glGetInteger(GL_CULL_FACE_MODE)
            set(value) = glCullFace(value)
        var depthTest: Boolean
            get() = glGetBoolean(GL_DEPTH_TEST)
            set(value) = setBool(GL_DEPTH_TEST, value)
        var depthClear: Double
            get() = glGetDouble(GL_DEPTH_CLEAR_VALUE)
            set(value) = glClearDepth(value)
        var depthFunc: Int
            get() = glGetInteger(GL_DEPTH_FUNC)
            set(value) = glDepthFunc(value)
        var depthRange: Vec2d
            get() = Vec2d(0, getDoubles(GL_DEPTH_RANGE, 2))
            set(value) = glDepthRange(value.x, value.y)
        var dither: Boolean
            get() = glGetBoolean(GL_DITHER)
            set(value) = setBool(GL_DITHER, value)
        val doubleBuffer: Boolean get() = glGetBoolean(GL_DOUBLEBUFFER)
        var smoothLine: Boolean
            get() = glGetBoolean(GL_LINE_SMOOTH)
            set(value) = setBool(GL_LINE_SMOOTH, value)
        var lineWidth: Float
            get() = glGetFloat(GL_LINE_WIDTH)
            set(value) = glLineWidth(value)
        var multiSample: Boolean
            get() = glGetBoolean(GL_MULTISAMPLE)
            set(value) = setBool(GL_MULTISAMPLE, value)
        var pointSize: Float
            get() = glGetFloat(GL_POINT_SIZE)
            set(value) = glPointSize(value)
        val sampleBuffers: Int get() = glGetInteger(GL_SAMPLE_BUFFERS)
        var stencilClear: Int
            get() = glGetInteger(GL_STENCIL_CLEAR_VALUE)
            set(value) = glClearStencil(value)
        val stencilFunc: Int get() = glGetInteger(GL_STENCIL_FUNC)
        val stencilRef: Int get() = glGetInteger(GL_STENCIL_REF)
        val stencilMask: Int get() = glGetInteger(GL_STENCIL_VALUE_MASK)
        var stencilFRM: Vec3i
            get() = Vec3i(stencilFunc, stencilRef, stencilMask)
            set(value) = glStencilFunc(value.x, value.y, value.z)
        var stencilTest
            get() = glGetBoolean(GL_STENCIL_TEST)
            set(value) = setBool(GL_STENCIL_TEST, value)
        val stereo: Boolean get() = glGetBoolean(GL_STEREO)
        val version: Vec2i get() = Vec2i(glGetInteger(GL_MAJOR_VERSION), glGetInteger(GL_MINOR_VERSION))
        var viewport
            get() = Vec4i(0, getInts(GL_VIEWPORT, 4))
            set(value) = glViewport(value.x, value.y, value.z, value.w)

        val maxViewPort: Vec2i get() = Vec2i(0, getInts(GL_MAX_VIEWPORT_DIMS, 0))

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

        fun setBool(pname: Int, value: Boolean) = if(value) glEnable(pname) else glDisable(pname)
    }
}