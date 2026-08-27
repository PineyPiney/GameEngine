package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL31C
import java.nio.FloatBuffer

open class OpenGlArrayMesh : OpenGlMesh {

	final override val attributes: Map<VertexAttribute<*, *>, Long>
	final override val count: Int

	constructor(vertices: FloatBuffer, attributes: Map<VertexAttribute<*, *>, Long>) : super() {
		this.attributes = attributes
		this.count = vertices.capacity() / attributes.keys.sumOf { it.size }
		bufferData(vertices, GL15C::glBufferData)
	}

	constructor(vertices: FloatBuffer, attributes: Iterable<VertexAttribute<*, *>>) : this(vertices, Mesh.createAttributes(attributes))

	constructor(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>) : super() {
		this.attributes = attributes
		this.count = vertices.size / attributes.keys.sumOf { it.size }
		bufferData(vertices, GL15C::glBufferData)
	}

	constructor(vertices: FloatArray, attributes: Iterable<VertexAttribute<*, *>>) : this(vertices, Mesh.createAttributes(attributes))

	constructor(VAO: Int, VBO: Int, attributes: Map<VertexAttribute<*, *>, Long>, count: Int) : super(VAO, VBO) {
		this.attributes = attributes
		this.count = count
	}

	fun <D> bufferData(data: D, func: (Int, D, Int) -> Unit) {
		if (GLFunc.isLoaded) {
			GL30C.glBindVertexArray(VAO)

			GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, VBO)
			func(GL15C.GL_ARRAY_BUFFER, data, GL15C.GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

			GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0)
			GL30C.glBindVertexArray(0)
		}
	}

	override fun draw(api: RenderingApi, mode: Int) {
		GL11C.glDrawArrays(mode, 0, count)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		GL31C.glDrawArraysInstanced(mode, 0, count, amount)
	}

	override fun delete() {
		super.delete()
		GL15C.glDeleteBuffers(VBO)
	}
}