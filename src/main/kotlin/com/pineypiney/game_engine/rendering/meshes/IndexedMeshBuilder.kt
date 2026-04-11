package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.models.ModelMesh
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4ub
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class IndexedMeshBuilder(val attributes: Set<VertexAttribute<*, *>>) {

	constructor(vararg attributes: VertexAttribute<*, *>) : this(attributes.toSet())

	val vertices = mutableListOf<MeshVertex>()
	val indices = mutableListOf<Int>()

	val currentVertex = attributes.map(VertexAttribute<*, *>::defaultPair).toTypedArray()
	var started = false

	var modifier: Modifier? = null

	val vertexSize = attributes.sumOf { it.bytes }

	fun vertex(x: Float, y: Float, z: Float): IndexedMeshBuilder {
		addVertex()
		addAttribute(VertexAttribute.POSITION, Vec3(x, y, z))
		return this
	}

	fun vertex(x: Number, y: Number, z: Number): IndexedMeshBuilder {
		return vertex(x.toFloat(), y.toFloat(), z.toFloat())
	}

	fun vertex(x: Float, y: Float): IndexedMeshBuilder {
		addVertex()
		addAttribute(VertexAttribute.POSITION2D, Vec2(x, y))
		return this
	}

	fun texture(u: Float, v: Float): IndexedMeshBuilder {
		addAttribute(VertexAttribute.TEX_COORD, Vec2(u, v))
		return this
	}

	fun texU(u: Float): IndexedMeshBuilder {
		addAttribute(VertexAttribute.TEX_U, u)
		return this
	}

	fun texV(v: Float): IndexedMeshBuilder {
		addAttribute(VertexAttribute.TEX_V, v)
		return this
	}

	fun colour(r: Float, g: Float, b: Float, a: Float): IndexedMeshBuilder {
		addAttribute(VertexAttribute.COLOUR, Vec4(r, g, b, a))
		return this
	}

	fun rgba(int: Int): IndexedMeshBuilder {
		if(attributes.contains(VertexAttribute.COLOUR)) return colour(((int shr 24) and 255) * _255, ((int shr 16) and 255) * _255, ((int shr 8) and 255) * _255, (int and 255) * _255)
		else {
			addAttribute(VertexAttribute.COLOUR_BYTES, Vec4ub(int shr 24, int shr 16, int shr 8, int))
			return this
		}
	}

	fun <T> addAttribute(attribute: VertexAttribute<T, *>, value: T) {
		val index = currentVertex.indexOfFirst { it.id == attribute }
		if(index != -1) currentVertex[index] = VertexAttribute.Pair(attribute, value)
	}

	fun startQuad(): IndexedMeshBuilder {
		addVertex()
		started = false
		modifier = Quad()
		return this
	}

	private fun addVertex() {
		if(started) {

			val index = vertices.indexOfFirst { it.vertexEquals(currentVertex) }
			if(index == -1){
				addIndex(vertices.size)
				vertices.add(MeshVertex(currentVertex.toSet()))
			}
			else addIndex(index)

			for ((i, v) in currentVertex.withIndex()) currentVertex[i] = v.id.defaultPair()
		}
		else started = true
	}

	private fun addIndex(index: Int) {
		modifier?.let {
			it.addIndex(index)
			if(it.complete()) {
				indices.addAll(it.allIndices())
				modifier = null
			}
		} ?: indices.add(index)
	}

	fun buildVertices(): ByteBuffer {
		addVertex()
		started = false
		val vertexBuffer = BufferUtils.createByteBuffer(vertexSize * vertices.size)
		for ((i, vertex) in vertices.withIndex()) {
			vertex.putData(vertexBuffer, i * vertexSize)
		}
		return vertexBuffer
	}

	fun build(): OpenGlIndexedMesh {
		addVertex()
		started = false
		val vertexBuffer = BufferUtils.createByteBuffer(vertexSize * vertices.size)
		for ((i, vertex) in vertices.withIndex()) {
			vertex.putData(vertexBuffer, i * vertexSize)
		}
		return OpenGlIndexedMesh(vertexBuffer, attributes, indices.toIntArray())
	}

	fun buildModel(id: String, factory: ResourceFactory): ModelMesh {
		addVertex()
		started = false
		return factory.createModelMesh(id, vertices.toTypedArray(), indices.toIntArray())
	}

	companion object {
		val _255 = 1f / 255f
	}

	abstract class Modifier {
		abstract fun addIndex(index: Int)
		abstract fun complete(): Boolean
		abstract fun allIndices(): Collection<Int>
	}

	class Quad : Modifier(){
		val indices = IntArray(4)
		var set = 0
		override fun addIndex(index: Int) {
			indices[set++] = index
		}
		override fun complete(): Boolean = set >= 4
		override fun allIndices(): Collection<Int> {
			return listOf(indices[0], indices[1], indices[2], indices[2], indices[3], indices[0])
		}
	}
}