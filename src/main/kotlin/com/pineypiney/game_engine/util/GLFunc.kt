package com.pineypiney.game_engine.util

import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import kool.ByteBuffer
import kool.DoubleBuffer
import kool.FloatBuffer
import kool.IntBuffer
import org.lwjgl.opengl.ARBImaging.GL_BLEND_COLOR
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL42C.*
import java.nio.ByteBuffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

@Suppress("UNUSED")
class GLFunc {

	companion object {

		val isLoaded
			get() = try {
				GL.getCapabilities()
				true
			} catch (_: IllegalStateException) {
				false
			}

		var activeTexture: Int
			get() = glGetInteger(GL_ACTIVE_TEXTURE)
			set(value) = glActiveTexture(value)
		var blend: Boolean
			get() = glGetBoolean(GL_BLEND)
			set(value) = setBool(GL_BLEND, value)
		val blendFuncSrc: Int get() = glGetInteger(GL_BLEND_SRC)
		val blendFuncDst: Int get() = glGetInteger(GL_BLEND_DST)

		val blendFuncSrcRGB: Int get() = glGetInteger(GL_BLEND_SRC_RGB)
		val blendFuncSrcA: Int get() = glGetInteger(GL_BLEND_SRC_ALPHA)
		val blendFuncDstRGB: Int get() = glGetInteger(GL_BLEND_DST_RGB)
		val blendFuncDstA: Int get() = glGetInteger(GL_BLEND_DST_ALPHA)
		var blendFuncSeparate: Vec4i
			get() = Vec4i(blendFuncSrcRGB, blendFuncSrcA, blendFuncDstRGB, blendFuncDstA)
			set(value) = glBlendFuncSeparate(value.x, value.y, value.z, value.w)
		var blendFunc: Vec2i
			get() = Vec2i(blendFuncSrc, blendFuncDst)
			set(value) = glBlendFunc(value.x, value.y)
		var blendEquationRGB: Int
			get() = glGetInteger(GL_BLEND_EQUATION_RGB)
			set(value) { glBlendEquationSeparate(value, blendEquationA)}
		var blendEquationA: Int
			get() = glGetInteger(GL_BLEND_EQUATION_ALPHA)
			set(value) { glBlendEquationSeparate(blendEquationRGB, value)}

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
		var frontFace
			get() = glGetInteger(GL_FRONT_FACE)
			set(value) = glFrontFace(value)
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
		var polygonMode: Int
			get() = glGetInteger(GL_POLYGON_MODE)
			set(value) { glPolygonMode(GL_FRONT_AND_BACK, value)}
		val sampleBuffers: Int get() = glGetInteger(GL_SAMPLE_BUFFERS)

		// Stencil Buffer
		var stencilClear: Int
			get() = glGetInteger(GL_STENCIL_CLEAR_VALUE)
			set(value) = glClearStencil(value)

		val stencilFunc: Int get() = glGetInteger(GL_STENCIL_FUNC)
		val stencilRef: Int get() = glGetInteger(GL_STENCIL_REF)
		val stencilMask: Int get() = glGetInteger(GL_STENCIL_VALUE_MASK)

		/**
		 * @param Vec3i.x Comparison function
		 * @param Vec3i.y Reference value
		 * @param Vec3i.z Comparison mask
		 */
		var stencilFRM: Vec3i
			get() = Vec3i(stencilFunc, stencilRef, stencilMask)
			set(value) = glStencilFunc(value.x, value.y, value.z)

		var stencilWriteMask: Int
			get() = glGetInteger(GL_STENCIL_WRITEMASK)
			set(value) = glStencilMask(value)
		val stencilOpFail: Int get() = glGetInteger(GL_STENCIL_FAIL)
		val stencilOpPassFail: Int get() = glGetInteger(GL_STENCIL_PASS_DEPTH_FAIL)
		val stencilOpPass: Int get() = glGetInteger(GL_STENCIL_PASS_DEPTH_PASS)

		/**
		 * @param Vec3i.x: Stencil fails
		 * @param Vec3i.y: Stencil passes, depth fails
		 * @param Vec3i.z: Stencil and Depth pass
		 */
		var stencilOp: Vec3i
			get() = Vec3i(stencilOpFail, stencilOpPassFail, stencilOpPass)
			set(value) {
				glStencilOp(value.x, value.y, value.z)
			}
		var stencilTest: Boolean
			get() = glGetBoolean(GL_STENCIL_TEST)
			set(value) = setBool(GL_STENCIL_TEST, value)
		val stencilBuffer: ByteBuffer
			get() {
				val viewport = viewport
				val buffer = ByteBuffer(viewport.z * viewport.w)
				glReadPixels(viewport.x, viewport.y, viewport.z, viewport.w, GL_STENCIL_INDEX, GL_UNSIGNED_BYTE, buffer)
				return buffer
			}

		val stereo: Boolean get() = glGetBoolean(GL_STEREO)
		val version: Vec2i get() = Vec2i(glGetInteger(GL_MAJOR_VERSION), glGetInteger(GL_MINOR_VERSION))
		var viewport
			get() = Vec4i(0, getInts(GL_VIEWPORT, 4))
			set(value) = glViewport(value.x, value.y, value.z, value.w)
		var viewportO: Vec2i
			get() = Vec2i(2, getInts(GL_VIEWPORT, 4))
			set(value) = glViewport(0, 0, value.x, value.y)

		val maxAtomicCounterFragment: Int get() = glGetInteger(GL_MAX_FRAGMENT_ATOMIC_COUNTERS)
		val maxViewPort: Vec2i get() = Vec2i(0, getInts(GL_MAX_VIEWPORT_DIMS, 0))

		fun getDataSize(dataType: Int): Int{
			return when(dataType){
				GL_BYTE, GL_UNSIGNED_BYTE -> 1
				GL_SHORT, GL_UNSIGNED_SHORT -> 2
				GL_INT, GL_UNSIGNED_INT, GL_FLOAT, GL_DOUBLE -> 4
				else -> 0
			}
		}

		fun getFloats(pname: Int, size: Int): FloatArray {
			val array = FloatArray(size)
			glGetFloatv(pname, array)

			return array
		}

		fun getDoubles(pname: Int, size: Int): DoubleArray {
			val array = DoubleArray(size)
			glGetDoublev(pname, array)

			return array
		}

		fun getInts(pname: Int, size: Int): IntArray {
			val array = IntArray(size)
			glGetIntegerv(pname, array)

			return array
		}

		fun getBools(pname: Int, size: Int): BooleanArray {
			val array = IntArray(size)
			glGetIntegerv(pname, array)

			return array.map { it != 0 }.toBooleanArray()
		}

		fun setBool(pname: Int, value: Boolean) = if (value) glEnable(pname) else glDisable(pname)

		fun getVec2i(handle: Long, func: (Long, IntBuffer, IntBuffer) -> Unit): Vec2i {
			val (wa, ha) = arrayOf(IntBuffer(1), IntBuffer(1))
			func(handle, wa, ha)
			return Vec2i(wa[0], ha[0])
		}

		fun setVec2i(handle: Long, func: (Long, Int, Int) -> Unit, value: Vec2i) {
			func(handle, value.x, value.y)
		}

		fun getVec2d(handle: Long, func: (Long, DoubleBuffer, DoubleBuffer) -> Unit): Vec2d {
			val (wa, ha) = arrayOf(DoubleBuffer(1), DoubleBuffer(1))
			func(handle, wa, ha)
			return Vec2d(wa[0], ha[0])
		}

		fun setVec2d(handle: Long, func: (Long, Double, Double) -> Unit, value: Vec2d) {
			func(handle, value.x, value.y)
		}

		fun getVec2(handle: Long, func: (Long, FloatBuffer, FloatBuffer) -> Unit): Vec2 {
			val (wa, ha) = arrayOf(FloatBuffer(1), FloatBuffer(1))
			func(handle, wa, ha)
			return Vec2(wa[0], ha[0])
		}

		fun getVec4i(handle: Long, func: (Long, IntBuffer, IntBuffer, IntBuffer, IntBuffer) -> Unit): Vec4i {
			val a = Array(4) { IntBuffer(1) }
			func(handle, a[0], a[1], a[2], a[3])
			return Vec4i(a[0][0], a[1][0], a[2][0], a[3][0])
		}

		fun versionAtLeast(major: Int, minor: Int): Boolean{
			val version = version
			return major > version.x || (major == version.x && minor >= version.y)
		}
	}
}