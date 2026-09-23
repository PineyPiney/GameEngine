package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlIndexedMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import glm_.quat.Quat
import glm_.vec3.Vec3

open class OpenGlModelMesh(
	override var id: String, override val vertices: Array<out MeshVertex>, override val indices: IntArray,
	override val material: ModelMaterial = PhongMaterial(id, emptyMap())
) : OpenGlIndexedMesh(MeshVertex.compile(vertices), vertices.firstOrNull()?.attributes ?: emptyList(), indices), ModelMesh {

	override var translation: Vec3 = Vec3()
	override var rotation: Quat = Quat()

	override fun toString(): String {
		return "ModelMesh[$id]"
	}

	override fun delete() {
		super.delete()
		material.delete()
	}
}

