package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.Vectors
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.extension_functions.put
import glm_.f
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.toBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import java.nio.ByteBuffer

interface Mesh : Deletable {

	val buffers: List<VertexBuffer>

	fun bind(api: RenderingApi)

	fun draw(api: RenderingApi, mode: Int = GL_TRIANGLES)

	fun drawInstanced(api: RenderingApi, amount: Int, mode: Int = GL_TRIANGLES)

	fun bindAndDraw(api: RenderingApi, mode: Int = GL_TRIANGLES) {
		bind(api)
		draw(api, mode)
	}

	fun hasAttribute(attribute: VertexAttribute<*, *>) = buffers.any { it.hasAttribute(attribute) }

	fun <A> getAttribute(attribute: VertexAttribute<A, *>): List<A> {
		val vBuffer = buffers.firstOrNull { it.hasAttribute(attribute) } ?: return emptyList()
		return vBuffer.getAttribute(attribute)
	}

	fun getBounds(transform: Mat4 = Mat4(1f)): Pair<Vec3, Vec3> {
		if (hasAttribute(VertexAttribute.POSITION2D)) {
			val poses = getAttribute(VertexAttribute.POSITION2D).map { Vec3(transform * Vec4(it, 0f, 1f)) }
			return Vectors.minMaxVec3(poses)
		} else if (hasAttribute(VertexAttribute.POSITION)) {
			val poses = getAttribute(VertexAttribute.POSITION).map { Vec3(transform * Vec4(it, 1f)) }
			return Vectors.minMaxVec3(poses)
		}
		else return Vec3() to Vec3()
	}

	object EmptyMesh : Mesh {
		override val buffers: List<VertexBuffer> = emptyList()

		override fun bind(api: RenderingApi) {}
		override fun draw(api: RenderingApi, mode: Int) {}
		override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {}
		override fun delete() {}
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

		fun textureQuad(factory: ResourceFactory, name: String, bl: Vec2, tr: Vec2, tbl: Vec2 = Vec2(0f), ttr: Vec2 = Vec2(1f)): Mesh {
			val positions = floatArrayOf(
				bl.x, bl.y,
				tr.x, bl.y,
				tr.x, tr.y,
				bl.x, tr.y,
			)
			val uvs = floatArrayOf(
				tbl.x, tbl.y,
				ttr.x, tbl.y,
				ttr.x, ttr.y,
				tbl.x, ttr.y,
			)

			val buffers = listOf<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>(
				positions.toBuffer() to mapOf(VertexAttribute.POSITION2D to 0L),
				uvs.toBuffer() to mapOf(VertexAttribute.TEX_COORD to 0L),
			)

			return factory.createIndexedMesh(name, buffers, intArrayOf(0, 1, 2, 0, 2, 3))
		}

		fun textureCircle(factory: ResourceFactory, name: String, center: Vec2, radius: Float, segments: Int = 32, tc: Vec2 = Vec2(.5f), tr: Float = .5f): Mesh {
			val positions = BufferUtils.createByteBuffer(segments * 8 + 8)
			val uvs = BufferUtils.createByteBuffer(segments * 8 + 8)
			positions.put(0, center)
			uvs.put(0, tc)
			val indices = IntArray(segments * 3)
			val angle = 2f * PIF / segments
			repeat(segments) { i ->
				val a = 2f * PIF - angle * i
				positions.put(i * 8 + 8, center + Vec2.fromAngle(a, radius))
				uvs.put(i * 8 + 8, tc + Vec2.fromAngle(a, tr))
				indices[i * 3] = 0
				indices[i * 3 + 1] = i + 1
				indices[i * 3 + 2] = ((i + 1) % segments) + 1
			}

			val buffers = listOf<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>(
				positions to mapOf(VertexAttribute.POSITION2D to 0L),
				uvs to mapOf(VertexAttribute.TEX_COORD to 0L),
			)

			return factory.createIndexedMesh(name, buffers, indices)
		}

		fun textureCuboid(factory: ResourceFactory, name: String, blf: Vec3, trb: Vec3, tbl: Vec2 = Vec2(0f), ttr: Vec2 = Vec2(1f)): Mesh {
			val positions = floatArrayOf(
				// Back
				trb.x, blf.y, blf.z,
				blf.x, blf.y, blf.z,
				blf.x, trb.y, blf.z,
				blf.x, trb.y, blf.z,
				trb.x, trb.y, blf.z,
				trb.x, blf.y, blf.z,

				// Front
				blf.x, blf.y, trb.z,
				trb.x, blf.y, trb.z,
				trb.x, trb.y, trb.z,
				trb.x, trb.y, trb.z,
				blf.x, trb.y, trb.z,
				blf.x, blf.y, trb.z,

				// Left
				blf.x, blf.y, blf.z,
				blf.x, blf.y, trb.z,
				blf.x, trb.y, trb.z,
				blf.x, trb.y, trb.z,
				blf.x, trb.y, blf.z,
				blf.x, blf.y, blf.z,

				// Right
				trb.x, blf.y, trb.z,
				trb.x, blf.y, blf.z,
				trb.x, trb.y, blf.z,
				trb.x, trb.y, blf.z,
				trb.x, trb.y, trb.z,
				trb.x, blf.y, trb.z,

				// Bottom
				blf.x, blf.y, blf.z,
				trb.x, blf.y, blf.z,
				trb.x, blf.y, trb.z,
				trb.x, blf.y, trb.z,
				blf.x, blf.y, trb.z,
				blf.x, blf.y, blf.z,

				// Top
				blf.x, trb.y, blf.z,
				blf.x, trb.y, trb.z,
				trb.x, trb.y, trb.z,
				trb.x, trb.y, trb.z,
				trb.x, trb.y, blf.z,
				blf.x, trb.y, blf.z,
			)
			val normalTex = floatArrayOf(
				// normals		 // texture co-ords
				0.0, 0.0, -1.0, ttr.x, tbl.y,
				0.0, 0.0, -1.0, ttr.x, ttr.y,
				0.0, 0.0, -1.0, tbl.x, ttr.y,
				0.0, 0.0, -1.0, tbl.x, ttr.y,
				0.0, 0.0, -1.0, tbl.x, tbl.y,
				0.0, 0.0, -1.0, ttr.x, tbl.y,
				0.0, 0.0, 1.0, tbl.x, tbl.y,
				0.0, 0.0, 1.0, tbl.x, ttr.y,
				0.0, 0.0, 1.0, ttr.x, ttr.y,
				0.0, 0.0, 1.0, ttr.x, ttr.y,
				0.0, 0.0, 1.0, ttr.x, tbl.y,
				0.0, 0.0, 1.0, tbl.x, tbl.y,
				-1.0, 0.0, 0.0, tbl.x, tbl.y,
				-1.0, 0.0, 0.0, tbl.x, ttr.y,
				-1.0, 0.0, 0.0, ttr.x, ttr.y,
				-1.0, 0.0, 0.0, ttr.x, ttr.y,
				-1.0, 0.0, 0.0, ttr.x, tbl.y,
				-1.0, 0.0, 0.0, tbl.x, tbl.y,
				1.0, 0.0, 0.0, tbl.x, tbl.y,
				1.0, 0.0, 0.0, tbl.x, ttr.y,
				1.0, 0.0, 0.0, ttr.x, ttr.y,
				1.0, 0.0, 0.0, ttr.x, ttr.y,
				1.0, 0.0, 0.0, ttr.x, tbl.y,
				1.0, 0.0, 0.0, tbl.x, tbl.y,
				0.0, -1.0, 0.0, tbl.x, tbl.y,
				0.0, -1.0, 0.0, tbl.x, ttr.y,
				0.0, -1.0, 0.0, ttr.x, ttr.y,
				0.0, -1.0, 0.0, ttr.x, ttr.y,
				0.0, -1.0, 0.0, ttr.x, tbl.y,
				0.0, -1.0, 0.0, tbl.x, tbl.y,
				0.0, 1.0, 0.0, tbl.x, tbl.y,
				0.0, 1.0, 0.0, tbl.x, ttr.y,
				0.0, 1.0, 0.0, ttr.x, ttr.y,
				0.0, 1.0, 0.0, ttr.x, ttr.y,
				0.0, 1.0, 0.0, ttr.x, tbl.y,
				0.0, 1.0, 0.0, tbl.x, tbl.y,
			)

			val buffers = listOf<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>(
				positions.toBuffer() to mapOf(VertexAttribute.POSITION to 0L),
				normalTex.toBuffer() to mapOf(
					VertexAttribute.NORMAL to 0L,
					VertexAttribute.TEX_COORD to 12L,
				),
			)

			return factory.createArrayMesh(name, buffers)
		}

		fun tessellatedPlane(factory: ResourceFactory, name: String, width: Float, height: Float, res: Int): Mesh {

			val o = Vec3(-width * .5f, 0f, height * .5f)
			val patchSize = Vec3(width, 0, -height) / res
			val texDelta = 1f / res
			val positions = FloatArray(res * res * 12)
			val uvs = FloatArray(res * res * 8)
			for (x in 0..<res) {
				val offsetX = x * res
				for (y in 0..<res) {
					val offset = offsetX + y
					(o + patchSize * Vec3(x, 0, y)).to(positions, offset * 12)
					(o + patchSize * Vec3(x + 1, 0, y)).to(positions, offset * 12 + 3)
					(o + patchSize * Vec3(x, 0, y + 1)).to(positions, offset * 12 + 6)
					(o + patchSize * Vec3(x + 1, 0, y + 1)).to(positions, offset * 12 + 9)

					(Vec2(x, y) * texDelta).to(uvs, offset * 8)
					(Vec2(x + 1, y) * texDelta).to(uvs, offset * 8 + 2)
					(Vec2(x, y + 1) * texDelta).to(uvs, offset * 8 + 4)
					(Vec2(x + 1, y + 1) * texDelta).to(uvs, offset * 8 + 6)
				}
			}

			val buffers = listOf<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>(
				positions.toBuffer() to mapOf(VertexAttribute.POSITION to 0L),
				uvs.toBuffer() to mapOf(VertexAttribute.TEX_COORD to 0L),
			)

			return factory.createArrayMesh(name, buffers)
		}

		lateinit var cornerSquareShape: Mesh; private set
		lateinit var centerSquareShape: Mesh; private set
		lateinit var screenQuadShape: Mesh; private set

		lateinit var cornerCubeShape: Mesh; private set
		lateinit var centerCubeShape: Mesh; private set

		fun initDefaultMeshes(factory: ResourceFactory) {
			cornerSquareShape = textureQuad(factory, "Corner Square", Vec2(), Vec2(1f))
			centerSquareShape = textureQuad(factory, "Center Square", Vec2(-0.5f), Vec2(0.5f))
			screenQuadShape = textureQuad(factory, "Screen Quad", Vec2(-1f), Vec2(1f))

			cornerCubeShape = textureCuboid(factory, "Corner Cube", Vec3(0f), Vec3(1f))
			centerCubeShape = textureCuboid(factory, "Center Cube", Vec3(-.5f), Vec3(.5f))

			DeletionQueue.GLOBAL.pushAll(cornerSquareShape, centerSquareShape, screenQuadShape, cornerCubeShape, centerCubeShape)
		}
	}
}