package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import glm_.quat.Quat
import glm_.vec3.Vec3
import java.nio.ByteBuffer

class VulkanModelMesh(vulkan: VulkanManager, override var id: String, override val vertices: Array<out MeshVertex>, indices: ByteBuffer, override val material: ModelMaterial = Model.brokeMaterial) :
	VulkanIndexedMesh(vulkan, MeshVertex.compile(vertices), indices, Mesh.createAttributes(vertices.firstOrNull()?.attributes?.toList() ?: emptyList())), ModelMesh {

	override var translation: Vec3 = Vec3()
	override var rotation: Quat = Quat()
	override var alpha = 1f
	override var order = 0

	override val indices = IntArray(indices.capacity() / 4)

	init {
		indices.position(0).asIntBuffer().get(this.indices)
	}
}