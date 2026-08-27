package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL43C
import java.nio.ByteBuffer

// https://wikis.khronos.org/opengl/Shader_Storage_Buffer_Object
class ShaderStorageBuffer(var size: Int, val binding: Int, val usage: Int) : Deletable {

	val SSBO = GL43C.glGenBuffers()

	init {
		resize(size)
	}

	fun bind(binding: Int = this.binding) {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, SSBO)
	}

	fun getData(offset: Long = 0L, size: Int = this.size): ByteBuffer {
		bind()
		val data = BufferUtils.createByteBuffer(size)
		GL43C.glGetBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, offset, data)
		return data
	}

	fun setData(data: ByteBuffer) {
		bind()
		GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, data, usage)
		size = data.capacity()
	}

	fun setSubData(data: ByteBuffer, offset: Long = 0L) {
		bind()
		GL43C.glBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, offset, data)
	}

	fun resize(size: Int) {
		bind()
		GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, size.toLong(), usage)
		this.size = size
	}

	override fun delete() {
		GL43C.glDeleteBuffers(SSBO)
	}
}