package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.rendering.meshes.VertexBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

class OpenGlVertexBuffer(override val stride: Int, override val attributes: Map<VertexAttribute<*, *>, Long>) : VertexBuffer() {

	constructor(attributes: Map<VertexAttribute<*, *>, Long>) : this(attributes.maxOf { it.value.toInt() + it.key.bytes }, attributes)

	val VBO = glGenBuffers()

	fun setAttributes(index: Int) {
		// How to read non-indices array

		var index = index
		for ((attrib, step) in attributes) {

			glEnableVertexAttribArray(index)
			when (attrib.type) {
				GL_FLOAT -> glVertexAttribPointer(index, attrib.size, GL_FLOAT, false, stride, step)
				GL_INT, GL_UNSIGNED_INT, GL_UNSIGNED_BYTE -> glVertexAttribIPointer(index, attrib.size, attrib.type, stride, step)
			}

			index++
		}
	}

	override fun getData(): ByteBuffer {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = BufferUtils.createByteBuffer(glGetBufferParameteri(GL_ARRAY_BUFFER, GL_BUFFER_SIZE))
		glGetBufferSubData(GL_ARRAY_BUFFER, 0L, buffer)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		return buffer
	}

	fun setData(data: ByteBuffer, index: Int) {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
		setAttributes(index)
	}

	fun <A> setAttribute(attribute: VertexAttribute<A, *>, values: List<A>) {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = BufferUtils.createByteBuffer(attribute.bytes)
		val step = attributes[attribute] ?: return
		for (i in 0..<values.size) {
			attribute.put(buffer, 0, values[i])
			glBufferSubData(GL_ARRAY_BUFFER, i * stride + step, buffer)
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0)
	}

	override fun delete() {
		glDeleteBuffers(VBO)
	}
}