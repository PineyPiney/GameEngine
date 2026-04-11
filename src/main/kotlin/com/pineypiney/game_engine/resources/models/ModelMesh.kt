package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3

interface ModelMesh : Mesh {

	var id: String
	val material: ModelMaterial
	var translation: Vec3
	var rotation: Quat
	var alpha: Float
	var order: Int

	val vertices: Array<out MeshVertex>
	val indices: IntArray

	val transform: Mat4 get() = I.translate(translation) * rotation.toMat4()

	fun setMaterialUniforms(shader: RenderShader) {
		material.apply(shader, "material")
	}

	fun reset() {

	}
}