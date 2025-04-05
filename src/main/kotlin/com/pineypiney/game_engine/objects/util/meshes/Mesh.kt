package com.pineypiney.game_engine.objects.util.meshes

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.getOrNull
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.f
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kool.ByteBuffer
import org.lwjgl.opengl.GL20C.glVertexAttribPointer
import org.lwjgl.opengl.GL30C.*

abstract class Mesh : Deleteable {

	val VAO = if (GLFunc.isLoaded) glGenVertexArrays() else -1
	val VBO = if (GLFunc.isLoaded) glGenBuffers() else -1

	abstract val attributes: Map<VertexAttribute<*>, Long>

	abstract val shape: Shape<*>

	abstract val count: Int
	val stride by lazy { attributes.keys.sumOf { it.size } * 4 }

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
			}

			index++
		}
	}

	fun <A> getAttribute(attribute: VertexAttribute<A>): List<A>{
		glBindBuffer(GL_ARRAY_BUFFER, VBO)
		val buffer = ByteBuffer(glGetBufferParameteri(GL_ARRAY_BUFFER, GL_BUFFER_SIZE))
		glGetBufferSubData(GL_ARRAY_BUFFER, 0L, buffer)
		val list = mutableListOf<A>()
		val step = attributes.getOrNull(attribute)?.toInt() ?: return emptyList()
		for(i in 0..<buffer.limit() / stride){
			list.add(attribute.get(buffer, i*stride + step))
		}
		return list
	}

	override fun delete() {
		glDeleteVertexArrays(VAO)
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
				i += attrib.size * 4L
			}
			return map
		}

		val cornerSquareShape = SquareMesh(Vec2(), Vec2(1f))
		val centerSquareShape = SquareMesh(Vec2(-0.5f), Vec2(0.5f))
		val screenQuadShape = SquareMesh(Vec2(-1f), Vec2(1f))
		val footSquare = SquareMesh(Vec2(-0.5f, 0f), Vec2(0.5f, 1f))

		val cornerCubeShape = CubeShape(Vec3(0f), Vec3(1f))
		val centerCubeShape = CubeShape(Vec3(-.5f), Vec3(.5f))
	}
}