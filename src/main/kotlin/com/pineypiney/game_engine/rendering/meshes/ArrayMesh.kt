package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL31C.*
import java.nio.FloatBuffer

open class ArrayMesh : OpenGlMesh {

	final override val attributes: Map<VertexAttribute<*, *>, Long>
	final override val count: Int

	constructor(vertices: FloatBuffer, attributes: Map<VertexAttribute<*, *>, Long>) : super() {
		this.attributes = attributes
		this.count = vertices.capacity() / attributes.keys.sumOf { it.size }
		bufferData(vertices, ::glBufferData)
	}

	constructor(vertices: FloatBuffer, attributes: Iterable<VertexAttribute<*, *>>) : this(vertices, Mesh.createAttributes(attributes))

	constructor(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>) : super() {
		this.attributes = attributes
		this.count = vertices.size / attributes.keys.sumOf { it.size }
		bufferData(vertices, ::glBufferData)
	}

	constructor(vertices: FloatArray, attributes: Iterable<VertexAttribute<*, *>>) : this(vertices, Mesh.createAttributes(attributes))

	constructor(VAO: Int, VBO: Int, attributes: Map<VertexAttribute<*, *>, Long>, count: Int) : super(VAO, VBO) {
		this.attributes = attributes
		this.count = count
	}

	fun <D> bufferData(data: D, func: (Int, D, Int) -> Unit) {
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			func(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindVertexArray(0)
		}
	}

	override fun draw(api: RenderingApi, mode: Int) {
		glDrawArrays(mode, 0, count)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		glDrawArraysInstanced(mode, 0, count, amount)
	}

	override fun delete() {
		super.delete()
		glDeleteBuffers(VBO)
	}
}