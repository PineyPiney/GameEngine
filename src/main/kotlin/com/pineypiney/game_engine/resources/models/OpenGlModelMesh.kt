package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlIndexedMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import glm_.quat.Quat
import glm_.vec3.Vec3
import java.nio.ByteBuffer

open class OpenGlModelMesh(
	override var id: String, buffers: List<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>, override val indices: IntArray,
	override val material: ModelMaterial = PhongMaterial(id, emptyMap())
) : OpenGlIndexedMesh(buffers, indices), ModelMesh {

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

