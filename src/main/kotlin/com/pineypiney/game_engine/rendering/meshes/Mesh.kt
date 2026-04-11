package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.util.Vectors
import com.pineypiney.game_engine.util.extension_functions.getOrNull
import glm_.f
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import java.nio.ByteBuffer

interface Mesh : Deletable {

	val attributes: Map<VertexAttribute<*, *>, Long>
	val stride: Int

	fun bind(api: RenderingApi)

	fun draw(api: RenderingApi, mode: Int = GL_TRIANGLES)

	fun drawInstanced(api: RenderingApi, amount: Int, mode: Int = GL_TRIANGLES)

	fun bindAndDraw(api: RenderingApi, mode: Int = GL_TRIANGLES) {
		bind(api)
		draw(api, mode)
	}

	fun getData(): ByteBuffer

	fun <A> getAttribute(attribute: VertexAttribute<A, *>): List<A> {
		val buffer = getData()
		val list = mutableListOf<A>()
		val step = attributes.getOrNull(attribute)?.toInt() ?: return emptyList()
		for(i in 0..<buffer.limit() / stride){
			list.add(attribute.get(buffer, i*stride + step))
		}
		return list
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

	object EmptyMesh : OpenGlMesh() {
		override val attributes: Map<VertexAttribute<*, *>, Long> = emptyMap()
		override val count: Int = 0

		override fun draw(api: RenderingApi, mode: Int) {}
		override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {}
	}

	companion object {
		fun createAttributes(iter: Iterable<VertexAttribute<*, *>>): Map<VertexAttribute<*, *>, Long> {
			val map = mutableMapOf<VertexAttribute<*, *>, Long>()
			var i = 0L
			for (attrib in iter) {
				map[attrib] = i
				i += attrib.bytes
			}
			return map
		}

		fun floatArrayOf(vararg elements: Number): FloatArray {
			return elements.map { it.f }.toFloatArray()
		}

		fun textureQuad(factory: ResourceFactory, bl: Vec2, tr: Vec2, tbl: Vec2 = Vec2(0f), ttr: Vec2 = Vec2(1f)): Mesh {
			val vertices = floatArrayOf(
				bl.x, bl.y, tbl.x, tbl.y,
				tr.x, bl.y, ttr.x, tbl.y,
				tr.x, tr.y, ttr.x, ttr.y,
				bl.x, tr.y, tbl.x, ttr.y,
			)
			return factory.createIndexedMesh(vertices, intArrayOf(0, 1, 2, 0, 2, 3), createAttributes(listOf(VertexAttribute.POSITION2D, VertexAttribute.TEX_COORD)))
		}

		fun textureCuboid(factory: ResourceFactory, blf: Vec3, trb: Vec3, tbl: Vec2 = Vec2(0f), ttr: Vec2 = Vec2(1f)): Mesh {
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
			return factory.createArrayMesh(vertices, createAttributes(setOf(VertexAttribute.POSITION, VertexAttribute.NORMAL, VertexAttribute.TEX_COORD)))
		}

		lateinit var cornerSquareShape: Mesh; private set
		lateinit var centerSquareShape: Mesh; private set
		lateinit var screenQuadShape: Mesh; private set
		lateinit var footSquare: Mesh; private set

		lateinit var cornerCubeShape: Mesh; private set
		lateinit var centerCubeShape: Mesh; private set

		fun initDefaultShapes(factory: ResourceFactory) {
			cornerSquareShape = textureQuad(factory, Vec2(), Vec2(1f))
			centerSquareShape = textureQuad(factory, Vec2(-0.5f), Vec2(0.5f))
			screenQuadShape = textureQuad(factory, Vec2(-1f), Vec2(1f))
			footSquare = textureQuad(factory, Vec2(-0.5f, 0f), Vec2(0.5f, 1f))

			cornerCubeShape = textureCuboid(factory, Vec3(0f), Vec3(1f))
			centerCubeShape = textureCuboid(factory, Vec3(-.5f), Vec3(.5f))
		}
	}
}