package com.pineypiney.game_engine.objects.util.meshes

import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL31C.*

abstract class IndicesMesh(vertices: FloatArray, attributes: Array<VertexAttribute<*>>, indices: IntArray) : Mesh() {

	private val EBO = if (GLFunc.isLoaded) glGenBuffers() else -1

	override val attributes: Map<VertexAttribute<*>, Long> = createAttributes(attributes)
	override val count: Int = indices.size

	init {
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
