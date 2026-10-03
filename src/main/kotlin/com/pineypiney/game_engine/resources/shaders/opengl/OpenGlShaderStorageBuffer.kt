package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.resources.shaders.ShaderStorageBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL43C
import java.nio.ByteBuffer

// https://wikis.khronos.org/opengl/Shader_Storage_Buffer_Object
class OpenGlShaderStorageBuffer(override var size: Int, val usage: Int) : ShaderStorageBuffer {

	val SSBO = GL43C.glGenBuffers()

	init {
		resize(size)
	}

	fun bind(binding: Int) {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, SSBO)
	}

	override fun getData(offset: Long, size: Int): ByteBuffer {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		val data = BufferUtils.createByteBuffer(size)
		GL43C.glGetBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, offset, data)
		return data
	}

	override fun setData(data: ByteBuffer) {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, data, usage)
		size = data.capacity()
	}

	override fun setSubData(data: ByteBuffer, offset: Long) {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		GL43C.glBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, offset, data)
	}

	override fun resize(size: Int) {
		GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, SSBO)
		GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, size.toLong(), usage)
		this.size = size
	}

	override fun delete() {
		GL43C.glDeleteBuffers(SSBO)
	}
}