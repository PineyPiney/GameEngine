package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL43C
import java.nio.ByteBuffer

// https://wikis.khronos.org/opengl/Shader_Storage_Buffer_Object
class ShaderStorageBuffer(val size: Int, val binding: Int, val usage: Int) : Deleteable {

	val SSBO = GL43C.glGenBuffers()

	init {
		bind()
		GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, size.toLong(), usage)
	}

	fun bind() {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, SSBO)
	}

	fun getData(offset: Long = 0L, size: Int = this.size): ByteBuffer {
		bind()
		val data = BufferUtils.createByteBuffer(size)
		GL43C.glGetBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, offset, data)
		return data
	}

	fun setData(data: ByteBuffer, offset: Long = 0L) {
		bind()
		if (data.capacity() == size && offset == 0L) {
			GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, data, usage)
		} else GL43C.glBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, offset, data)
	}

	override fun delete() {
		GL43C.glDeleteBuffers(SSBO)
	}
}