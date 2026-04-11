package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.OpenGlIndexedMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import glm_.quat.Quat
import glm_.vec3.Vec3

open class OpenGlModelMesh(
	override var id: String, override val vertices: Array<out MeshVertex>, override val indices: IntArray, val defaultAlpha: Float = 1f,
	val defaultOrder: Int = 0, override val material: ModelMaterial = Model.brokeMaterial
) : OpenGlIndexedMesh(MeshVertex.compile(vertices), vertices.firstOrNull()?.attributes ?: emptyList(), indices), ModelMesh {

	override var translation: Vec3 = Vec3()
	override var rotation: Quat = Quat()
	override var alpha = defaultAlpha
	override var order = defaultOrder

	override fun reset() {
		this.alpha = this.defaultAlpha
		this.order = this.defaultOrder
	}

	override fun toString(): String {
		return "ModelMesh[$id]"
	}

	companion object {

		private val v1 = MeshVertex.builder(0f, 0f, 0f).tex(0f, 0f).build()
		private val v2 = MeshVertex.builder(1f, 0f, 0f).tex(1f, 0f).build()
		private val v3 = MeshVertex.builder(1f, 1f, 0f).tex(1f, 1f).build()
		private val v4 = MeshVertex.builder(0f, 1f, 0f).tex(0f, 1f).build()

		val default = OpenGlModelMesh("brokeMesh", arrayOf(v1, v2, v3, v4), intArrayOf(0, 3, 2, 2, 1, 0))

		var indicesMult = 1f
	}

}

