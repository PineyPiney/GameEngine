package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.xr.XrUtil.processResult
import glm_.vec3.Vec3i
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer
import java.nio.IntBuffer

object LwjglUtil {

	fun version(long: Long) = Vec3i(long shr 48, (long shr 32) and 0xffff, long and 0xffffffff)

	fun <E> enumerate(stack: MemoryStack, name: String, creator: (MemoryStack, Int) -> E, func: (IntBuffer, E?) -> Int) : E? {
		val b = stack.callocInt(1)
		processResult(func(b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(stack, b[0])
			processResult(func(b, result), "Failed to get $name")
			return result
		}
		return null
	}

	fun <A, E> enumerate(stack: MemoryStack, name: String, instance: A, creator: (MemoryStack, Int) -> E, func: (A, IntBuffer, E?) -> Int) : E? {
		val b = stack.callocInt(1)
		processResult(func(instance, b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(stack, b[0])
			processResult(func(instance, b, result), "Failed to get $name")
			return result
		}
		return null
	}

	fun <A, B, E> enumerate(stack: MemoryStack, name: String, instance: A, device: B, creator: (MemoryStack, Int) -> E, func: (A, B, IntBuffer, E?) -> Int) : E? {
		val b = stack.callocInt(1)
		processResult(func(instance, device, b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(stack, b[0])
			processResult(func(instance, device, b, result), "Failed to get $name")
			return result
		}
		return null
	}

	fun <A, S: Struct<S>, SB: StructBuffer<S, SB>> enumerateStructs(stack: MemoryStack, instance: A, creator: (Int, MemoryStack) -> SB, func: (A, IntBuffer, SB?) -> Unit): SB? {
		val b = stack.mallocInt(1)
		func(instance, b, null)

		if (b[0] > 0) {
			val result = creator(b[0], stack)
			func(instance, b, result)
			return result
		}
		return null
	}

	fun <S: Struct<S>, SB: StructBuffer<S, SB>> enumerateStructs(stack: MemoryStack, name: String, creator: (Int, MemoryStack) -> SB, func: (IntBuffer, SB?) -> Int) : SB? {
		val b = stack.callocInt(1)
		processResult(func(b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(b[0], stack)
			processResult(func(b, result), "Failed to get $name")
			return  result
		}
		return null
	}

	fun <S: Struct<S>, SB: StructBuffer<S, SB>> enumerateStructs(stack: MemoryStack, name: String, setType: S.() -> Unit, creator: (Int, MemoryStack) -> SB, func: (IntBuffer, SB?) -> Int) : SB? {
		val b = stack.callocInt(1)
		processResult(func(b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(b[0], stack)
			for(s in result) setType(s)
			processResult(func(b, result), "Failed to get $name")
			return  result
		}
		return null
	}

	fun <A, S: Struct<S>, SB: StructBuffer<S, SB>> enumerateStructs(stack: MemoryStack, name: String, instance: A, creator: (Int, MemoryStack) -> SB, func: (A, IntBuffer, SB?) -> Int) : SB? {
		val b = stack.callocInt(1)
		processResult(func(instance, b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(b[0], stack)
			processResult(func(instance, b, result), "Failed to get $name")
			return result
		}
		return null
	}

	fun <A, B, S: Struct<S>, SB: StructBuffer<S, SB>> enumerateStructs(stack: MemoryStack, name: String, instance: A, device: B, creator: (Int, MemoryStack) -> SB, func: (A, B, IntBuffer, SB?) -> Int) : SB? {
		val b = stack.callocInt(1)
		processResult(func(instance, device, b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(b[0], stack)
			processResult(func(instance, device, b, result), "Failed to get $name")
			return result
		}
		return null
	}

	fun <A, B, S: Struct<S>, SB: StructBuffer<S, SB>> enumerateStructs(name: String, instance: A, device: B, creator: (Int) -> SB, func: (A, B, IntBuffer, SB?) -> Int) : SB? {
		val b = MemoryUtil.memCallocInt(1)
		processResult(func(instance, device, b, null), "Failed to get the number of $name")

		if (b[0] > 0) {
			val result = creator(b[0])
			processResult(func(instance, device, b, result), "Failed to get $name")
			MemoryUtil.memFree(b)
			return result
		}
		MemoryUtil.memFree(b)
		return null
	}
}