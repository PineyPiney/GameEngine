package com.pineypiney.game_engine.util

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Pointer

object LwjglUtil {

	fun <E> createPointerBuffer(stack: MemoryStack, elements: Collection<E>, getAddress: (E) -> Long): PointerBuffer {
		val p = stack.mallocPointer(elements.size)
		for(element in elements) p.put(getAddress(element))
		return p.flip()
	}

	fun createPointerBuffer(stack: MemoryStack, elements: Collection<Pointer>): PointerBuffer {
		val p = stack.mallocPointer(elements.size)
		for(element in elements) p.put(element.address())
		return p.flip()
	}
}