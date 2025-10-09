package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.resources.models.pgm.Controller
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4ub
import kool.ByteBuffer
import kool.emptyByteBuffer
import java.nio.ByteBuffer

class MeshVertex(val map: Set<VertexAttribute.Pair<*>>) {

	val position: Vec3
		get() = map.firstOrNull { it.id == VertexAttribute.POSITION }?.value as? Vec3 ?: Vec3(map.first { it.id == VertexAttribute.POSITION2D }.value as Vec2)

	val attributes: Array<VertexAttribute<*>>
		get() = map.map { it.id }.toTypedArray()

	fun putData(buffer: ByteBuffer, offset: Int){
		var off = offset
		for(pair in map){
			pair.set(buffer, off)
			off += pair.id.bytes
		}
	}

	fun vertexEquals(array: Array<VertexAttribute.Pair<*>>): Boolean{
		return map.all { (a, v) -> v == (array.firstOrNull { it.id == a }?.value ?: return false) }
	}

	override fun toString(): String {
		return "ModularVertex[${map.joinToString()}]"
	}

	companion object {
		fun builder() = Builder()
		fun builder(pos: Vec3) = Builder(pos)
		fun builder(x: Float, y: Float, z: Float) = Builder(Vec3(x, y, z))

		fun compile(vertices: Array<out MeshVertex>): ByteBuffer{
			if(vertices.isEmpty()) return emptyByteBuffer()

			val stride = vertices.first().attributes.sumOf { it.bytes }
			val buffer = ByteBuffer(vertices.size * stride)
			for(i in 0..<vertices.size){
				vertices[i].putData(buffer, i * stride)
			}
			return buffer
		}
	}

	class Builder(){
		constructor(pos: Vec3): this() {
			attributes.add(VertexAttribute.POSITION pair pos)
		}
		constructor(pos: Vec2): this() {
			attributes.add(VertexAttribute.POSITION2D pair pos)
		}

		val attributes = mutableSetOf<VertexAttribute.Pair<*>>()

		fun tex(tex: Vec2): Builder {
			attributes.add(VertexAttribute.TEX_COORD pair tex)
			return this
		}
		fun tex(x: Float, y: Float): Builder {
			attributes.add(VertexAttribute.TEX_COORD pair Vec2(x, y))
			return this
		}
		fun normal(nor: Vec3): Builder {
			attributes.add(VertexAttribute.NORMAL pair nor)
			return this
		}
		fun normal(x: Float, y: Float, z: Float): Builder {
			attributes.add(VertexAttribute.NORMAL pair Vec3(x, y, z))
			return this
		}
		fun tangent(tan: Vec3): Builder {
			attributes.add(VertexAttribute.TANGENT pair tan)
			return this
		}
		fun tangent(x: Float, y: Float, z: Float): Builder {
			attributes.add(VertexAttribute.TANGENT pair Vec3(x, y, z))
			return this
		}
		fun weights(bones: Array<Controller.BoneWeight>): Builder{
			val ids = Vec4ub()
			val weights = Vec4()
			for(i in 0..3){
				val bone = bones.getOrNull(i)
				ids[i] = bone?.id ?: -1
				weights[i] = bone?.weight ?: 0f
			}
			attributes.add(VertexAttribute.BONE_IDS pair ids)
			attributes.add(VertexAttribute.BONE_WEIGHTS pair weights)
			return this
		}
		fun fillBones(): Builder{
			attributes.add(VertexAttribute.BONE_IDS.defaultPair())
			attributes.add(VertexAttribute.BONE_WEIGHTS.defaultPair())
			return this
		}
		fun build() = MeshVertex(attributes.toSet())
	}
}