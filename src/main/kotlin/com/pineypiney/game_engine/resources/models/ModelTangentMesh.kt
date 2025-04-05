package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.util.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.pgm.Controller
import com.pineypiney.game_engine.util.extension_functions.putVec2
import com.pineypiney.game_engine.util.extension_functions.putVec3
import com.pineypiney.game_engine.util.extension_functions.putVec4
import com.pineypiney.game_engine.util.extension_functions.putVec4i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import java.nio.ByteBuffer

class ModelTangentMesh(id: String, vertices: Array<TangentMeshVertex>, indices: IntArray, defaultAlpha: Float = 1f, defaultOrder: Int = 1, material: ModelMaterial = Model.brokeMaterial): ModelMesh(id, vertices, indices, defaultAlpha, defaultOrder, material) {

	override val attributes: Map<VertexAttribute<*>, Long> get() = Companion.attributes

	companion object {
		val attributes = createAttributes(arrayOf(
			VertexAttribute.POSITION, VertexAttribute.NORMAL, VertexAttribute.TEX_COORD,
			VertexAttribute.TANGENT, VertexAttribute.BONE_IDS, VertexAttribute.BONE_WEIGHTS
		))
	}

	class TangentMeshVertex(position: Vec3, tex: Vec2, normal: Vec3, val tangent: Vec3, weights: Array<Controller.BoneWeight> = arrayOf()): MeshVertex(position, tex, normal, weights){

		override fun putData(buffer: ByteBuffer, offset: Int) {
			buffer.putVec3(offset, position)
				.putVec3(offset + 12, normal)
				.putVec2(offset + 24, texCoord)
				.putVec3(offset + 32, tangent)
				.putVec4i(offset + 44, Vec4i{ weights.getOrNull(it)?.id ?: -1})
				.putVec4(offset + 60, Vec4{ weights.getOrNull(it)?.weight ?: 0f})
		}
	}
}