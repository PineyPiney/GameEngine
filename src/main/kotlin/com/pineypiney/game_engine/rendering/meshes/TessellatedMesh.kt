package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.PIF
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kool.toFloatArray
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL40C
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TessellatedMesh: ArrayMesh {

	val patchVertices: Int

	constructor(vertices: FloatArray, attributes: Map<VertexAttribute<*>, Long>, patchVertices: Int): super(vertices, attributes){
		this.patchVertices = patchVertices
	}

	constructor(parent: ArrayMesh, patchVertices: Int): super(parent.VAO, parent.VBO, parent.attributes, parent.count){
		this.patchVertices = patchVertices
	}

	override fun draw(mode: Int) {
		GLFunc.patchVertices = this.patchVertices
		super.draw(GL40C.GL_PATCHES)
	}

	companion object {

		fun generatePlane(width: Float, height: Float, res: Int): TessellatedMesh {

			val o = Vec3(-width * .5f, 0f, height * .5f)
			val patchSize = Vec3(width, 0, -height) / res
			val texDelta = 1f / res
			val vertices = FloatBuffer.allocate(res * res * 20)
			for (x in 0..<res) {
				val offsetX = x * res * 20
				for (y in 0..<res) {
					val offset = offsetX + y * 20
					(o + patchSize * Vec3(x, 0, y)).to(vertices, offset)
					(Vec2(x, y) * texDelta).to(vertices, offset + 3)
					(o + patchSize * Vec3(x + 1, 0, y)).to(vertices, offset + 5)
					(Vec2(x + 1, y) * texDelta).to(vertices, offset + 8)
					(o + patchSize * Vec3(x, 0, y + 1)).to(vertices, offset + 10)
					(Vec2(x, y + 1) * texDelta).to(vertices, offset + 13)
					(o + patchSize * Vec3(x + 1, 0, y + 1)).to(vertices, offset + 15)
					(Vec2(x + 1, y + 1) * texDelta).to(vertices, offset + 18)
				}
			}
			return TessellatedMesh(ArrayMesh(vertices.toFloatArray(), arrayOf(VertexAttribute.POSITION, VertexAttribute.TEX_COORD)), 4)
		}

		fun generateIcoSphere(radius: Float, res: Int): TessellatedMesh {

			val phi = (sqrt(5f) + 1f) * .5f
			val s = 2f / (sqrt(phi * phi + 1f))
			val ringY = (2f - s * s) * .5f * radius
			val ringXZ = sqrt(radius * radius - ringY * ringY)

			val c72 = ringXZ * cos(.4f * PIF)
			val s72 = ringXZ * sin(.4f * PIF)
			val c144 = ringXZ * cos(.2 * PIF)
			val s144 = ringXZ * sin(.8f * PIF)

			val orientatedIcoPoints = arrayOf(
				// Bottom Point
				Vec3(0f, -radius, 0f),

				// Lower Ring
				Vec3(0f, -ringY, -ringXZ),
				Vec3(s72, -ringY, -c72),
				Vec3(s144, -ringY, c144),
				Vec3(-s144, -ringY, c144),
				Vec3(-s72, -ringY, -c72),

				// Upper Ring
				Vec3(s144, ringY, -c144),
				Vec3(s72, ringY, c72),
				Vec3(0f, ringY, ringXZ),
				Vec3(-s72, ringY, c72),
				Vec3(-s144, ringY, -c144),

				// Top Point
				Vec3(0f, radius, 0f),
			)

			val triangles = intArrayOf(
				0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 1,
				1, 6, 2, 2, 6, 7, 2, 7, 3, 3, 7, 8, 3, 8, 4,
				4, 8, 9, 4, 9, 5, 5, 9, 10, 5, 10, 1, 1, 10, 6,
				6, 11, 7, 7, 11, 8, 8, 11, 9, 9, 11, 10, 10, 11, 6
			)

			val trianglesPerFace = res * res
			// 9 floats per triangle, 20 faces
			val buffer = BufferUtils.createFloatBuffer(9 * trianglesPerFace * 20)

			for (face in 0..19) {
				var faceOffset = 9 * trianglesPerFace * face
				val p0 = orientatedIcoPoints[triangles[face * 3]]
				val p1 = orientatedIcoPoints[triangles[face * 3 + 1]]
				val p2 = orientatedIcoPoints[triangles[face * 3 + 2]]

				val vx = (p1 - p0) / res
				val vy = (p2 - p0) / res
				for (row in 0..<res) {
					// Number of triangles on this row
					val rowTris = 2 * (res - row) - 1
					// First point on this row
					val pr = p0 + (vy * row)

					val points = arrayOf(pr, pr + vx, pr + vy)
					for (t in 0..<rowTris) {
						(points[0].normalize() * radius).to(buffer, faceOffset)
						if (t % 2 == 0) {
							(points[1].normalize() * radius).to(buffer, faceOffset + 3)
							(points[2].normalize() * radius).to(buffer, faceOffset + 6)
						} else {
							(points[2].normalize() * radius).to(buffer, faceOffset + 3)
							(points[1].normalize() * radius).to(buffer, faceOffset + 6)
						}
						faceOffset += 9

						val i = (-t).mod(3)
						val j = (i + 2) % 3
						points[i] = points[j] + vx
					}
				}
			}

			return TessellatedMesh(ArrayMesh(buffer, arrayOf(VertexAttribute.POSITION)), 3)
		}
	}
}