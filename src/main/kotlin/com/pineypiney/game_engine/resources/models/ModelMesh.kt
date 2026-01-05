package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.IndicesMesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3

// Meshes are made up of faces, which are in turn made up of MeshVertices.
// Mesh vertices are each associated with a position, normal and texMap,
// as well as up to 4 bone weights. The transformation of each vertex is linearly
// interpolated from these 4 bone weights in the shader

open class ModelMesh(
	var id: String, val vertices: Array<out MeshVertex>, val indices: IntArray, val defaultAlpha: Float = 1f,
	val defaultOrder: Int = 0, val material: ModelMaterial = Model.brokeMaterial
) : IndicesMesh(MeshVertex.compile(vertices), vertices.firstOrNull()?.attributes ?: emptyArray(), indices) {

	var translation: Vec3 = Vec3()
	var rotation: Quat = Quat()
	var alpha = defaultAlpha
	var order = defaultOrder

	val transform: Mat4 get() = I.translate(translation) * rotation.toMat4()

	fun setMaterialUniforms(shader: RenderShader) {
		material.apply(shader, "material")
	}

	fun reset() {
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

		val default = ModelMesh("brokeMesh", arrayOf(v1, v2, v3, v4), intArrayOf(0, 3, 2, 2, 1, 0))

		var indicesMult = 1f
	}

}

