package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

abstract class OpenGlMesh(val VAO: Int, val VBO: Int) : Mesh {

	constructor() : this(if (GLFunc.isLoaded) glGenVertexArrays() else -1, if (GLFunc.isLoaded) glGenBuffers() else -1)

	abstract val count: Int
	override val stride by lazy { attributes.keys.sumOf { it.bytes } }

	open fun bind() {
		glBindVertexArray(this.VAO)
	}

	fun bindAndDraw(api: RenderingApi, mode: Int = GL_TRIANGLES) {
		bind()
		draw(api, mode)
	}

	fun setAttributes() {
		// How to read non-indices array

		var index = 0
		for ((attrib, step) in attributes) {

			glEnableVertexAttribArray(index)
			when (attrib.type) {
				GL_FLOAT -> glVertexAttribPointer(index, attrib.size, GL_FLOAT, false, stride, step)
				GL_INT, GL_UNSIGNED_INT, GL_UNSIGNED_BYTE -> glVertexAttribIPointer(index, attrib.size, attrib.type, stride, step)
			}

			index++
		}
	}

	fun getBufferSize(): Int {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val size = glGetBufferParameteri(GL_ARRAY_BUFFER, GL_BUFFER_SIZE)
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		return size
	}

	override fun getData(): ByteBuffer {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = BufferUtils.createByteBuffer(glGetBufferParameteri(GL_ARRAY_BUFFER, GL_BUFFER_SIZE))
		glGetBufferSubData(GL_ARRAY_BUFFER, 0L, buffer)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		return buffer
	}

	fun <A> setAttribute(attribute: VertexAttribute<A, *>, values: List<A>) {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = BufferUtils.createByteBuffer(attribute.bytes)
		val step = attributes[attribute] ?: return
		for (i in 0..<values.size) {
			attribute.set(buffer, 0, values[i])
			glBufferSubData(GL_ARRAY_BUFFER, i * stride + step, buffer)
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0)
	}

	override fun delete() {
		glDeleteVertexArrays(VAO)
	}
}