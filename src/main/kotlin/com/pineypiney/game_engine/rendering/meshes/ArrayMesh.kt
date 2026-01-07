package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL31C.*

open class ArrayMesh : Mesh {

	final override val attributes: Map<VertexAttribute<*>, Long>
	final override val count: Int

	constructor(vertices: FloatArray, attributes: Map<VertexAttribute<*>, Long>): super(){
		this.attributes = attributes
		this.count = vertices.size / attributes.keys.sumOf { it.size }

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

	constructor(vertices: FloatArray, attributes: Array<VertexAttribute<*>>): this(vertices, createAttributes(attributes))

	constructor(VAO: Int, VBO: Int, attributes: Map<VertexAttribute<*>, Long>, count: Int): super(VAO, VBO){
		this.attributes = attributes
		this.count = count
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