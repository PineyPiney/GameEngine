package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.pgm.Controller
import com.pineypiney.game_engine.util.extension_functions.expand
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL11C.GL_INT
import org.lwjgl.opengl.GL15C.*

class ModelTangentMesh(id: String, vertices: Array<TangentMeshVertex>, indices: IntArray, defaultAlpha: Float = 1f, defaultOrder: Int = 1, material: ModelMaterial = Model.brokeMaterial): ModelMesh(id, vertices, indices, defaultAlpha, defaultOrder, material) {

	override fun setupFloats() {
		// Buffer floats
		glBindBuffer(GL_ARRAY_BUFFER, floatVBO)


		setAttribs(
			mapOf(
				0 to Pair(GL_FLOAT, 3),
				1 to Pair(GL_FLOAT, 3),
				2 to Pair(GL_FLOAT, 2),
				3 to Pair(GL_FLOAT, 3),
				5 to Pair(GL_FLOAT, 4)
			)
		)

		// Get data from each vertex and put it in one long array
		val floatArray = vertices.flatMap(MeshVertex::getFloatData).toFloatArray()
		// Send the data to the buffers
		glBufferData(GL_ARRAY_BUFFER, floatArray, GL_STATIC_DRAW)

	}

	override fun setupInts() {

		// Buffer ints
		glBindBuffer(GL_ARRAY_BUFFER, intVBO)

		setAttribs(mapOf(4 to Pair(GL_INT, 4)))

		// Get data from each vertex and put it in one long array
		val intArray = vertices.flatMap(MeshVertex::getIntData).toIntArray()
		// Send the data to the buffers
		glBufferData(GL_ARRAY_BUFFER, intArray, GL_STATIC_DRAW)
	}

	class TangentMeshVertex(position: Vec3, tex: Vec2, normal: Vec3, val tangent: Vec3, weights: Array<Controller.BoneWeight> = arrayOf()): MeshVertex(position, tex, normal, weights){

		override fun getFloatData(): List<Float> {
			return position.run { listOf(x, y, z) } +
					normal.run { listOf(x, y, z) } +
					texCoord.run { listOf(x, y) } +
					tangent.run { listOf(x, y, z) } +
					weights.map { w -> w.weight }.expand(4)
		}
	}
}