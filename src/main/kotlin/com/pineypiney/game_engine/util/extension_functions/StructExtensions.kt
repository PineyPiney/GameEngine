package com.pineypiney.game_engine.util.extension_functions

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer

fun <S : Struct<S>, SB : StructBuffer<S, SB>> MutableCollection<S>.toBuffer(create: (Int) -> SB): SB {
	val buffer = create(size)
	for (element in this) buffer.put(element)
	return buffer.flip()
}

fun <S : Struct<S>, SB : StructBuffer<S, SB>> MutableCollection<S>.toBuffer(stack: MemoryStack, create: (Int, MemoryStack) -> SB): SB {
	val buffer = create(size, stack)
	for (element in this) buffer.put(element)
	return buffer.flip()
}

fun <S : Struct<S>> MutableCollection<S>.free() {
	forEach(Struct<*>::free)
	clear()
}