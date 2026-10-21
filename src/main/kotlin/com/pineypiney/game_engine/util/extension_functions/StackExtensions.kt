package com.pineypiney.game_engine.util.extension_functions

import kool.emptyLongBuffer
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Pointer
import java.nio.IntBuffer
import java.nio.LongBuffer


fun <E> MemoryStack.ptr(elements: Collection<E>, getAddress: (E) -> Long): PointerBuffer {
	val p = mallocPointer(elements.size)
	for(element in elements) p.put(getAddress(element))
	return p.flip()
}

fun MemoryStack.ptr(element: Pointer): PointerBuffer {
	return mallocPointer(1).put(0, element.address())
}

fun MemoryStack.ptr(elements: Collection<Pointer>): PointerBuffer {
	val p = mallocPointer(elements.size)
	for(element in elements) p.put(element.address())
	return p.flip()
}

fun MemoryStack.long(element: Long): LongBuffer {
	return mallocLong(1).put(0, element)
}

fun MemoryStack.longOrEmpty(element: Long?): LongBuffer {
	return if(element == null) emptyLongBuffer()
	else mallocLong(1).put(0, element)
}

fun MemoryStack.int(element: Int): IntBuffer {
	return mallocInt(1).put(0, element)
}

fun MemoryStack.stringsToPointer(strings: Collection<String>): PointerBuffer {
	val pointer = mallocPointer(strings.size)
	for(str in strings) pointer.put(UTF8(str, true))
	return pointer.flip()
}