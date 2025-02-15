package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.pgm.Controller
import com.pineypiney.game_engine.resources.models.pgm.Face
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.extension_functions.expand
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL31C.*

// Meshes are made up of faces, which are in turn made up of MeshVertices.
// Mesh vertices are each associated with a position, normal and texMap,
// as well as up to 4 bone weights. The transformation of each vertex is linearly
// interpolated from these 4 bone weights in the shader

open class ModelMesh(
	var id: String, val vertices: Array<out MeshVertex>, val indices: IntArray, val defaultAlpha: Float = 1f,
	val defaultOrder: Int = 0, val material: ModelMaterial = Model.brokeMaterial
) : Mesh() {

	constructor(id: String, faces: Array<Face>, material: ModelMaterial = Model.brokeMaterial) :
			this(
				id,
				faces.flatMap { it.vertices.toList() }.toTypedArray(),
				(0 until faces.size * 3).toSet().toIntArray(),
				material = material
			)

	init {
		vertexSize = 12
		positionSize = 3
		normalSize = 3
		normalOffset = 3
		textureSize = 2
		textureOffset = 6
	}

	override val shape: Shape2D
		get() = Rect2D(Vec2(), Vec2(1f)) //TODO
	override val count: Int = indices.size

	protected val floatVBO = glGenBuffers()
	protected val intVBO = glGenBuffers()
	protected val EBO = glGenBuffers()

	var translation: Vec3 = Vec3()
	var rotation: Quat = Quat()
	var alpha = defaultAlpha
	var order = defaultOrder

	val transform: Mat4 get() = I.translate(translation) * rotation.toMat4()

	init {
		if (vertices.isNotEmpty()) setupMesh()
	}

	override fun bind() {
		super.bind()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
	}

	override fun draw(mode: Int) {
		glDrawElements(mode, count, GL_UNSIGNED_INT, 0)

	}

	override fun drawInstanced(amount: Int, mode: Int) {
		glDrawElementsInstanced(mode, count, GL_UNSIGNED_INT, 0, amount)
	}

	private fun setupMesh() {

		// Bind Buffers
		glBindVertexArray(VAO)

		setupFloats()
		setupInts()

		setupElements()

		// Clean up
		glBindVertexArray(0)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
	}

	open fun setupFloats() {

		// Buffer floats
		glBindBuffer(GL_ARRAY_BUFFER, floatVBO)


		setAttribs(
			mapOf(
				0 to Pair(GL_FLOAT, 3),
				1 to Pair(GL_FLOAT, 3),
				2 to Pair(GL_FLOAT, 2),
				4 to Pair(GL_FLOAT, 4)
			)
		)

		// Get data from each vertex and put it in one long array
		val floatArray = vertices.flatMap(MeshVertex::getFloatData).toFloatArray()
		// Send the data to the buffers
		glBufferData(GL_ARRAY_BUFFER, floatArray, GL_STATIC_DRAW)
	}

	open fun setupInts() {

		// Buffer ints
		glBindBuffer(GL_ARRAY_BUFFER, intVBO)

		setAttribs(mapOf(3 to Pair(GL_INT, 4)))

		// Get data from each vertex and put it in one long array
		val intArray = vertices.flatMap(MeshVertex::getIntData).toIntArray()
		// Send the data to the buffers
		glBufferData(GL_ARRAY_BUFFER, intArray, GL_STATIC_DRAW)
	}

	fun setupElements() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
	}

	fun setMaterialUniforms(shader: Shader) {
		material.apply(shader, "material")
	}

	fun reset() {
		this.alpha = this.defaultAlpha
		this.order = this.defaultOrder
	}

	override fun getVertices(): FloatArray {
		return getFloatBuffer(floatVBO, GL_ARRAY_BUFFER)
	}

	override fun delete() {
		super.delete()
		glDeleteBuffers(intArrayOf(floatVBO, intVBO, EBO))
	}

	companion object {

		private val v1 = MeshVertex(Vec3(0, 0, 0), Vec2(0, 0))
		private val v2 = MeshVertex(Vec3(1, 0, 0), Vec2(1, 0))
		private val v3 = MeshVertex(Vec3(1, 1, 0), Vec2(1, 1))
		private val v4 = MeshVertex(Vec3(0, 1, 0), Vec2(0, 1))

		val default = ModelMesh("brokeMesh", arrayOf(v1, v2, v3, v4), intArrayOf(0, 3, 2, 2, 1, 0))

		var indicesMult = 1f
	}

	open class MeshVertex(
		val position: Vec3,
		val texCoord: Vec2 = Vec2(),
		val normal: Vec3 = Vec3(0, 0, 1),
		val weights: Array<Controller.BoneWeight> = arrayOf()
	) : Copyable<MeshVertex>,
		Deleteable {

		open fun getFloatData(): List<Float> {
			return position.run { listOf(x, y, z) } +
					normal.run { listOf(x, y, z) } +
					texCoord.run { listOf(x, y) } +
					weights.map { w -> w.weight }.expand(4)
		}

		open fun getIntData(): List<Int> {
			// When the shader reaches a bone index of -1 it breaks the loop
			return weights.map { w -> w.id }.expand(4, -1)
		}

		override fun copy(): MeshVertex {
			return MeshVertex(position.copy(), texCoord.copy(), normal.copy(), weights.copy())
		}

		override fun delete() {

		}

		override fun toString(): String {
			return "[$position, $texCoord]"
		}
	}
}