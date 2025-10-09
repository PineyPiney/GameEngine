package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.resources.models.ModelMesh
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4ub
import kool.ByteBuffer

class IndicesMeshBuilder(val attributes: Set<VertexAttribute<*>>) {

	constructor(vararg attributes: VertexAttribute<*>): this(attributes.toSet())

	val vertices = mutableListOf<MeshVertex>()
	val indices = mutableListOf<Int>()

	val currentVertex = attributes.map(VertexAttribute<*>::defaultPair).toTypedArray()
	var started = false

	var modifier: Modifier? = null

	val vertexSize = attributes.sumOf { it.bytes }

	fun vertex(x: Float, y: Float, z: Float): IndicesMeshBuilder{
		addVertex()
		addAttribute(VertexAttribute.POSITION, Vec3(x, y, z))
		return this
	}

	fun vertex(x: Number, y: Number, z: Number): IndicesMeshBuilder{
		return vertex(x.toFloat(), y.toFloat(), z.toFloat())
	}

	fun vertex(x: Float, y: Float): IndicesMeshBuilder{
		addVertex()
		addAttribute(VertexAttribute.POSITION2D, Vec2(x, y))
		return this
	}

	fun texture(u: Float, v: Float): IndicesMeshBuilder{
		addAttribute(VertexAttribute.TEX_COORD, Vec2(u, v))
		return this
	}

	fun colour(r: Float, g: Float, b: Float, a: Float): IndicesMeshBuilder{
		addAttribute(VertexAttribute.COLOUR, Vec4(r, g, b, a))
		return this
	}

	fun rgba(int: Int): IndicesMeshBuilder{
		if(attributes.contains(VertexAttribute.COLOUR)) return colour(((int shr 24) and 255) * _255, ((int shr 16) and 255) * _255, ((int shr 8) and 255) * _255, (int and 255) * _255)
		else {
			addAttribute(VertexAttribute.COLOUR_BYTES, Vec4ub(int shr 24, int shr 16, int shr 8, int))
			return this
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun <T> addAttribute(attribute: VertexAttribute<T>, value: T){
		val index = currentVertex.indexOfFirst { it.id == attribute }
		if(index != -1) currentVertex[index] = VertexAttribute.Pair(attribute, value)
	}

	fun quad(): IndicesMeshBuilder{
		addVertex()
		started = false
		modifier = Quad()
		return this
	}

	fun addVertex(){
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

	fun addIndex(index: Int){
		modifier?.let {
			it.addIndex(index)
			if(it.complete()) {
				indices.addAll(it.allIndices())
				modifier = null
			}
		} ?: indices.add(index)
	}

	fun build(): IndicesMesh {
		addVertex()
		started = false
		val vertexBuffer = ByteBuffer(vertexSize * vertices.size)
		for ((i, vertex) in vertices.withIndex()) {
			vertex.putData(vertexBuffer, i * vertexSize)
		}
		return IndicesMesh(vertexBuffer, attributes.toTypedArray(), indices.toIntArray())
	}

	fun buildModel(id: String): ModelMesh {
		addVertex()
		started = false
		return ModelMesh(id, vertices.toTypedArray(), indices.toIntArray())
	}

	companion object {
		val _255 = 1f / 255f
	}

	abstract class Modifier(){
		abstract fun addIndex(index: Int)
		abstract fun complete(): Boolean
		abstract fun allIndices(): Collection<Int>
	}

	class Quad(): Modifier(){
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