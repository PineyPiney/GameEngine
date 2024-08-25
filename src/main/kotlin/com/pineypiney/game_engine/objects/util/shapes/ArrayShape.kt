package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL31C.*

abstract class ArrayShape(vertices: FloatArray, parts: IntArray) : Mesh() {

	override val count = vertices.size / parts.sum()
	val VBO = if (GLFunc.isLoaded) glGenBuffers() else -1

	init {
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttribs(parts)

			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindVertexArray(0)
		}
	}

	override fun draw(mode: Int) {
		glDrawArrays(mode, 0, count)
	}

	override fun drawInstanced(amount: Int, mode: Int) {
		glDrawArraysInstanced(mode, 0, count, amount)
	}

	override fun getVertices(): FloatArray {
		return getFloatBuffer(VBO, GL_ARRAY_BUFFER)
	}

	override fun delete() {
		super.delete()
		glDeleteBuffers(VBO)
	}
}