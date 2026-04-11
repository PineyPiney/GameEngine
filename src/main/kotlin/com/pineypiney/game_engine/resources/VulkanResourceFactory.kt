package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.textures.TextureI
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.vulkan.VulkanIndexedMesh
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.vulkan.VulkanModelMesh
import kool.toBuffer
import org.lwjgl.BufferUtils

class VulkanResourceFactory(val vulkan: VulkanManager) : ResourceFactory() {

	override fun createSubShader(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String) {
		ShaderLoader.INSTANCE.loadShaderVulkan(vulkan, loader, ResourceKey(fileName.removeSuffix(".$suf")), fileName, code, stage.vulkan)
	}

	override fun createShader() {
		TODO("Not yet implemented")
	}

	override fun createTexture(): TextureI {
		TODO("Not yet implemented")
	}

	override fun createArrayMesh(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		val stride = attributes.entries.last().run { value + key.bytes }.toInt()
		val indices = BufferUtils.createByteBuffer(4 * vertices.size / stride)
		repeat(indices.capacity() / 4) { indices.putInt(it) }
		val mesh = VulkanIndexedMesh(vulkan, vertices.toBuffer(), indices.flip(), attributes)
		vulkan.deletionQueue.push(mesh)
		return mesh
	}

	override fun createIndexedMesh(vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		val mesh = VulkanIndexedMesh(vulkan, vertices.toBuffer(), indices.toBuffer(), attributes)
		vulkan.deletionQueue.push(mesh)
		return mesh
	}

	override fun createModelMesh(id: String, vertices: Array<out MeshVertex>, indices: IntArray, alpha: Float, order: Int, material: ModelMaterial): ModelMesh {
		val newLayout = setOf(VertexAttribute.POSITION, VertexAttribute.TEX_U, VertexAttribute.NORMAL, VertexAttribute.TEX_V, VertexAttribute.COLOUR)
		val newVertices = vertices.map { it.convert(newLayout) }.toTypedArray()
		val mesh = VulkanModelMesh(vulkan, id, newVertices, indices.toBuffer(), material)
		vulkan.deletionQueue.push(mesh)
		return mesh
	}
}