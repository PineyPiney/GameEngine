package com.pineypiney.game_engine.util

import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import org.lwjgl.system.MemoryStack
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

object NativeFuncs {

	fun getInt(handle: Long, func: (Long, IntBuffer) -> Unit): Int {
		MemoryStack.stackPush().use { stack ->
			val x = stack.mallocInt(1)
			func(handle, x)
			return x[0]
		}
	}

	fun getVec2i(handle: Long, func: (Long, IntBuffer, IntBuffer) -> Unit): Vec2i {
		MemoryStack.stackPush().use { stack ->
			val x = stack.mallocInt(1)
			val y = stack.mallocInt(1)
			func(handle, x, y)
			return Vec2i(x[0], y[0])
		}
	}

	fun setVec2i(handle: Long, func: (Long, Int, Int) -> Unit, value: Vec2i) {
		func(handle, value.x, value.y)
	}

	fun getVec2(func: (FloatBuffer, FloatBuffer) -> Unit): Vec2 {
		MemoryStack.stackPush().use { stack ->
			val x = stack.mallocFloat(1)
			val y = stack.mallocFloat(1)
			func(x, y)
			return Vec2(x[0], y[0])
		}
	}

	fun getVec2(handle: Long, func: (Long, FloatBuffer, FloatBuffer) -> Unit): Vec2 {
		MemoryStack.stackPush().use { stack ->
			val x = stack.mallocFloat(1)
			val y = stack.mallocFloat(1)
			func(handle, x, y)
			return Vec2(x[0], y[0])
		}
	}

	fun setVec2(handle: Long, func: (Long, Float, Float) -> Unit, value: Vec2) {
		func(handle, value.x, value.y)
	}

	fun getVec2d(handle: Long, func: (Long, DoubleBuffer, DoubleBuffer) -> Unit): Vec2d {
		MemoryStack.stackPush().use { stack ->
			val x = stack.mallocDouble(1)
			val y = stack.mallocDouble(1)
			func(handle, x, y)
			return Vec2d(x[0], y[0])
		}
	}

	fun setVec2d(handle: Long, func: (Long, Double, Double) -> Unit, value: Vec2d) {
		func(handle, value.x, value.y)
	}

	fun getVec4i(handle: Long, func: (Long, IntBuffer, IntBuffer, IntBuffer, IntBuffer) -> Unit): Vec4i {
		MemoryStack.stackPush().use { stack ->
			val x = stack.mallocInt(1)
			val y = stack.mallocInt(1)
			val z = stack.mallocInt(1)
			val w = stack.mallocInt(1)
			func(handle, x, y, z, w)
			return Vec4i(x[0], y[0], z[0], w[0])
		}
	}

}