package com.pineypiney.game_engine.resources.shaders

import kool.ByteBuffer
import kool.IntBuffer
import kool.toIntArray
import org.lwjgl.opengl.GL43C

class AtomicCounter(val size: Int) {

	val buffer = GL43C.glGenBuffers()

	init {
		bind()
		GL43C.glBufferData(GL43C.GL_ATOMIC_COUNTER_BUFFER, 4L * size, GL43C.GL_DYNAMIC_DRAW)
	}

	fun bind(){
		GL43C.glBindBuffer(GL43C.GL_ATOMIC_COUNTER_BUFFER, buffer)
	}

	fun bind(binding: Int){
		GL43C.glBindBuffer(GL43C.GL_ATOMIC_COUNTER_BUFFER, 0)
		GL43C.glBindBufferBase(GL43C.GL_ATOMIC_COUNTER_BUFFER, binding, buffer)
	}

	fun retrieveValues(): IntArray {
		bind()
		val buffer = GL43C.glMapBufferRange(GL43C.GL_ATOMIC_COUNTER_BUFFER, 0, 4L * size, GL43C.GL_MAP_WRITE_BIT or GL43C.GL_MAP_INVALIDATE_BUFFER_BIT or GL43C.GL_MAP_UNSYNCHRONIZED_BIT) ?: return intArrayOf()
		return buffer.asIntBuffer().toIntArray()
	}

	fun getValues(): IntArray {
		val buffer = IntBuffer(size)
		GL43C.glGetBufferSubData(GL43C.GL_ATOMIC_COUNTER_BUFFER, 0L, buffer)
		return buffer.toIntArray()
	}

	fun setValues(values: IntArray, offset: Long = 0L){
		GL43C.glBufferSubData(GL43C.GL_ATOMIC_COUNTER_BUFFER, offset, values)
	}

	fun setValue(value: Int, index: Int = 0){
		val buffer = ByteBuffer(4).putInt(value).position(0)
		GL43C.glBufferSubData(GL43C.GL_ATOMIC_COUNTER_BUFFER, index * 4L, buffer)
	}

	fun reset(){
		bind()
		GL43C.glBufferSubData(GL43C.GL_ATOMIC_COUNTER_BUFFER, 0L, IntArray(size))
	}

	companion object {
		fun unbind() = GL43C.glBindBuffer(GL43C.GL_ATOMIC_COUNTER_BUFFER, 0)
	}
}