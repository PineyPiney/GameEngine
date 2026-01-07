package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.Vectors
import com.pineypiney.game_engine.util.extension_functions.getOrNull
import glm_.f
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.ByteBuffer
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

abstract class Mesh(val VAO: Int, val VBO: Int) : Deleteable {

	constructor(): this(if (GLFunc.isLoaded) glGenVertexArrays() else -1, if (GLFunc.isLoaded) glGenBuffers() else -1)

	abstract val attributes: Map<VertexAttribute<*>, Long>

	abstract val count: Int
	val stride by lazy { attributes.keys.sumOf { it.bytes } }

	open fun bind() {
		glBindVertexArray(this.VAO)
	}

	abstract fun draw(mode: Int = GL_TRIANGLES)

	fun bindAndDraw(mode: Int = GL_TRIANGLES) {
		bind()
		draw(mode)
	}

	abstract fun drawInstanced(amount: Int, mode: Int = GL_TRIANGLES)

	fun setAttributes(){
		// How to read non-indices array

		var index = 0
		for ((attrib, step) in attributes) {

			glEnableVertexAttribArray(index)
			when (attrib.type) {
				GL_FLOAT -> glVertexAttribPointer(index, attrib.size, GL_FLOAT, false, stride, step)
				GL_INT -> glVertexAttribIPointer(index, attrib.size, GL_INT, stride, step)
                GL_UNSIGNED_BYTE -> glVertexAttribIPointer(index, attrib.size, GL_UNSIGNED_BYTE, stride, step)
			}

			index++
		}
	}

	fun getBufferSize(): Int{
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val size = glGetBufferParameteri(GL_ARRAY_BUFFER, GL_BUFFER_SIZE)
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		return size
	}

	fun getData(): ByteBuffer {
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = ByteBuffer(glGetBufferParameteri(GL_ARRAY_BUFFER, GL_BUFFER_SIZE))
		glGetBufferSubData(GL_ARRAY_BUFFER, 0L, buffer)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		return buffer
	}

	fun <A> getAttribute(attribute: VertexAttribute<A>): List<A>{
		val buffer = getData()
		val list = mutableListOf<A>()
		val step = attributes.getOrNull(attribute)?.toInt() ?: return emptyList()
		for(i in 0..<buffer.limit() / stride){
			list.add(attribute.get(buffer, i*stride + step))
		}
		return list
	}

	fun <A> setAttribute(attribute: VertexAttribute<A>, values: List<A>){
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = ByteBuffer(attribute.bytes)
		val step = attributes[attribute] ?: return
		for(i in 0..<values.size){
			attribute.set(buffer, 0, values[i])
			glBufferSubData(GL_ARRAY_BUFFER, i * stride + step, buffer)
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0)
	}

	override fun delete() {
		glDeleteVertexArrays(VAO)
	}

	fun getBounds(transform: Mat4 = Mat4(1f)): Pair<Vec3, Vec3> {
		if(attributes.contains(VertexAttribute.POSITION2D)){
			val poses = getAttribute(VertexAttribute.POSITION2D).map { Vec3(transform * Vec4(it, 0f, 1f)) }
			return Vectors.minMaxVec3(poses)
		}
		else if(attributes.contains(VertexAttribute.POSITION)){
			val poses = getAttribute(VertexAttribute.POSITION).map { Vec3(transform * Vec4(it, 1f)) }
			return Vectors.minMaxVec3(poses)
		}
		else return Vec3() to Vec3()
	}

	object EmptyMesh : Mesh(){
		override val attributes: Map<VertexAttribute<*>, Long> = emptyMap()
		override val count: Int = 0

		override fun draw(mode: Int) {}
		override fun drawInstanced(amount: Int, mode: Int) {}

	}

	companion object {

		fun floatArrayOf(vararg elements: Number): FloatArray {
			return elements.map { it.f }.toFloatArray()
		}

		fun createAttributes(array: Array<VertexAttribute<*>>): Map<VertexAttribute<*>, Long>{
			val map = mutableMapOf<VertexAttribute<*>, Long>()
			var i = 0L
			for(attrib in array){
				map[attrib] = i
				i += attrib.bytes
			}
			return map
		}

		fun textureQuad(bl: Vec2, tr: Vec2, tbl: Vec2 = Vec2(0f), ttr: Vec2 = Vec2(1f)): IndicesMesh{
			val vertices = floatArrayOf(
				bl.x, bl.y, tbl.x, tbl.y,
				bl.x, tr.y, tbl.x, ttr.y,
				tr.x, tr.y, ttr.x, ttr.y,
				tr.x, bl.y, ttr.x, tbl.y
			)
			return IndicesMesh(vertices, arrayOf(VertexAttribute.POSITION2D, VertexAttribute.TEX_COORD), intArrayOf(0, 1, 2, 0, 2, 3))
		}

		fun textureCuboid(blf: Vec3, trb: Vec3, tbl: Vec2 = Vec2(0f), ttr: Vec2 = Vec2(1f)): ArrayMesh{
			val vertices = floatArrayOf(
				// positions		 // normals		 // texture co-ords
				// Back
				trb.x, blf.y, blf.z, 0.0, 0.0, -1.0, ttr.x, tbl.y,
				blf.x, blf.y, blf.z, 0.0, 0.0, -1.0, ttr.x, ttr.y,
				blf.x, trb.y, blf.z, 0.0, 0.0, -1.0, tbl.x, ttr.y,
				blf.x, trb.y, blf.z, 0.0, 0.0, -1.0, tbl.x, ttr.y,
				trb.x, trb.y, blf.z, 0.0, 0.0, -1.0, tbl.x, tbl.y,
				trb.x, blf.y, blf.z, 0.0, 0.0, -1.0, ttr.x, tbl.y,

				// Front
				blf.x, blf.y, trb.z, 0.0, 0.0, 1.0, tbl.x, tbl.y,
				trb.x, blf.y, trb.z, 0.0, 0.0, 1.0, tbl.x, ttr.y,
				trb.x, trb.y, trb.z, 0.0, 0.0, 1.0, ttr.x, ttr.y,
				trb.x, trb.y, trb.z, 0.0, 0.0, 1.0, ttr.x, ttr.y,
				blf.x, trb.y, trb.z, 0.0, 0.0, 1.0, ttr.x, tbl.y,
				blf.x, blf.y, trb.z, 0.0, 0.0, 1.0, tbl.x, tbl.y,

				// Left
				blf.x, blf.y, blf.z, 1.0, 0.0, 0.0, tbl.x, tbl.y,
				blf.x, blf.y, trb.z, 1.0, 0.0, 0.0, tbl.x, ttr.y,
				blf.x, trb.y, trb.z, 1.0, 0.0, 0.0, ttr.x, ttr.y,
				blf.x, trb.y, trb.z, 1.0, 0.0, 0.0, ttr.x, ttr.y,
				blf.x, trb.y, blf.z, 1.0, 0.0, 0.0, ttr.x, tbl.y,
				blf.x, blf.y, blf.z, 1.0, 0.0, 0.0, tbl.x, tbl.y,

				// Right
				trb.x, blf.y, trb.z, 1.0, 0.0, 1.0, tbl.x, tbl.y,
				trb.x, blf.y, blf.z, 1.0, 0.0, 1.0, tbl.x, ttr.y,
				trb.x, trb.y, blf.z, 1.0, 0.0, 1.0, ttr.x, ttr.y,
				trb.x, trb.y, blf.z, 1.0, 0.0, 1.0, ttr.x, ttr.y,
				trb.x, trb.y, trb.z, 1.0, 0.0, 1.0, ttr.x, tbl.y,
				trb.x, blf.y, trb.z, 1.0, 0.0, 1.0, tbl.x, tbl.y,

				// Bottom
				blf.x, blf.y, blf.z, 0.0, -1.0, 0.0, tbl.x, tbl.y,
				trb.x, blf.y, blf.z, 0.0, -1.0, 0.0, tbl.x, ttr.y,
				trb.x, blf.y, trb.z, 0.0, -1.0, 0.0, ttr.x, ttr.y,
				trb.x, blf.y, trb.z, 0.0, -1.0, 0.0, ttr.x, ttr.y,
				blf.x, blf.y, trb.z, 0.0, -1.0, 0.0, ttr.x, tbl.y,
				blf.x, blf.y, blf.z, 0.0, -1.0, 0.0, tbl.x, tbl.y,

				// Top
				blf.x, trb.y, blf.z, 0.0, 1.0, 0.0, tbl.x, tbl.y,
				blf.x, trb.y, trb.z, 0.0, 1.0, 0.0, tbl.x, ttr.y,
				trb.x, trb.y, trb.z, 0.0, 1.0, 0.0, ttr.x, ttr.y,
				trb.x, trb.y, trb.z, 0.0, 1.0, 0.0, ttr.x, ttr.y,
				trb.x, trb.y, blf.z, 0.0, 1.0, 0.0, ttr.x, tbl.y,
				blf.x, trb.y, blf.z, 0.0, 1.0, 0.0, tbl.x, tbl.y,
			)

			return ArrayMesh(vertices, arrayOf(VertexAttribute.POSITION, VertexAttribute.NORMAL, VertexAttribute.TEX_COORD))
		}


		val cornerSquareShape = textureQuad(Vec2(), Vec2(1f))
		val centerSquareShape = textureQuad(Vec2(-0.5f), Vec2(0.5f))
		val screenQuadShape = textureQuad(Vec2(-1f), Vec2(1f))
		val footSquare = textureQuad(Vec2(-0.5f, 0f), Vec2(0.5f, 1f))

		val cornerCubeShape = textureCuboid(Vec3(0f), Vec3(1f))
		val centerCubeShape = textureCuboid(Vec3(-.5f), Vec3(.5f))
	}
}