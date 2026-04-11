package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.textures.TextureI

abstract class ResourceFactory {

	init {
		INSTANCE = this
	}

	abstract fun createSubShader(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String)
	abstract fun createShader()
	abstract fun createTexture(): TextureI
	abstract fun createArrayMesh(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createIndexedMesh(vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createModelMesh(id: String, vertices: Array<out MeshVertex>, indices: IntArray, alpha: Float = 1f, order: Int = 0, material: ModelMaterial = Model.brokeMaterial): ModelMesh

	companion object {
		lateinit var INSTANCE: ResourceFactory; private set
	}
}