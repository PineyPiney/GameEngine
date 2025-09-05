package com.pineypiney.game_engine.objects.util.meshes

import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL31C.*

abstract class ArrayMesh(vertices: FloatArray, override val attributes: Map<VertexAttribute<*>, Long>) : Mesh() {

	constructor(vertices: FloatArray, attributes: Array<VertexAttribute<*>>): this(vertices, createAttributes(attributes))

	override val count = vertices.size / attributes.keys.sumOf { it.size }

	init {
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

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

	override fun delete() {
		super.delete()
		glDeleteBuffers(VBO)
	}
}