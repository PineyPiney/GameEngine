package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import kool.ByteBuffer
import kool.cap
import org.lwjgl.opengl.GL31C.*
import java.nio.ByteBuffer

open class IndicesMesh protected constructor(attributes: Array<VertexAttribute<*>>, indices: IntArray) : Mesh() {

	private val EBO = if (GLFunc.isLoaded) glGenBuffers() else -1

	override val attributes: Map<VertexAttribute<*>, Long> = createAttributes(attributes)
	override val count: Int = indices.size

	constructor(vertices: FloatArray, attributes: Array<VertexAttribute<*>>, indices: IntArray): this(attributes, indices){
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

			glBindVertexArray(0)
			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		}
	}

	constructor(vertices: ByteBuffer, attributes: Array<VertexAttribute<*>>, indices: IntArray): this(attributes, indices){
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

			glBindVertexArray(0)
			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		}
	}

	fun getIndices(): ByteBuffer {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
		val buffer = ByteBuffer(glGetBufferParameteri(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_SIZE))
		glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0L, buffer)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		return buffer
	}

	fun getIndicesInts(): IntArray {
		val buffer = getIndices().asIntBuffer()
		val array = IntArray(buffer.cap)
		buffer.get(array)
		return array
	}

	override fun bind() {
		super.bind()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
	}

	override fun draw(mode: Int) {
		glDrawElements(mode, count, GL_UNSIGNED_INT, 0)
	}

	override fun drawInstanced(amount: Int, mode: Int) {
		glDrawElementsInstanced(mode, count, GL_UNSIGNED_INT, 0, amount)
	}

	override fun delete() {
		super.delete()
		glDeleteBuffers(VBO)
		glDeleteBuffers(EBO)
	}
}
