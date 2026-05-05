package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlIndexedMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import glm_.quat.Quat
import glm_.vec3.Vec3

open class OpenGlModelMesh(
	override var id: String, override val vertices: Array<out MeshVertex>, override val indices: IntArray, val defaultAlpha: Float = 1f,
	val defaultOrder: Int = 0, override val material: ModelMaterial = PhongMaterial(id, emptyMap())
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

	override fun delete() {
		super.delete()
		material.delete()
	}
}

