package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.rendering.meshes.vulkan.VulkanIndexedMesh
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.VulkanModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage3D
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanManager
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import kool.toBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.vulkan.VK10
import java.nio.ByteBuffer

class VulkanResourceFactory(val vulkan: VulkanManager) : ResourceFactory() {

	override fun nullTexture2D(): Texture2D = VulkanImage2D(vulkan.device, "null", 0, 0, TextureFormat.R8, 0, 0, 0)
	override fun nullTexture3D(): Texture3D = VulkanImage3D(vulkan.device, "null", 0, 0, TextureFormat.R8, 0, 0, 0, 0)

	override fun createSubShader(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String) {
		ShaderLoader.INSTANCE.loadShaderVulkan(vulkan, loader, ResourceKey(fileName.removeSuffix(".$suf")), fileName, code, stage)
	}

	override fun createShader() {
		TODO("Not yet implemented")
	}

	override fun createTexture2D(name: String, width: Int, height: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): Texture2D {
		val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
				VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
				params.usage.vulkan

		val image = VkUtil.createImage(vulkan.device, name, VK10.VK_IMAGE_TYPE_2D, format, usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, Vec2i(width, height), params)
		if (data != null) vulkan.submitter.submitImmediate { cmd ->
			image.uploadData(cmd, format, data)
		}
		return image
	}

	override fun createTexture3D(name: String, width: Int, height: Int, depth: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): Texture3D {
		val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
				VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
				params.usage.vulkan

		val image = VkUtil.createImage3D(vulkan.device, name, VK10.VK_IMAGE_TYPE_2D, format, usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, Vec3i(width, height, depth), params)
		if (data != null) vulkan.submitter.submitImmediate { cmd -> image.uploadData(cmd, format, data) }
		return image
	}

	override fun createArrayMesh(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		val stride = attributes.entries.last().run { value + key.bytes }.toInt()
		val indices = BufferUtils.createByteBuffer(4 * vertices.size / stride)
		repeat(indices.capacity() / 4) { indices.putInt(it) }
		val mesh = VulkanIndexedMesh(vulkan, vertices.toBuffer(), indices.flip(), attributes)
		vulkan.deletionQueue.push(mesh)
		return mesh
	}

	override fun createIndexedMesh(vertices: ByteBuffer, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		val mesh = VulkanIndexedMesh(vulkan, vertices, indices.toBuffer(), attributes)
		vulkan.deletionQueue.push(mesh)
		return mesh
	}

	override fun createIndexedMesh(vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		return createIndexedMesh(vertices.toBuffer(), indices, attributes)
	}

	override fun createModelMesh(id: String, vertices: Array<out MeshVertex>, indices: IntArray, alpha: Float, order: Int, material: ModelMaterial): ModelMesh {
		val newLayout = setOf(VertexAttribute.POSITION, VertexAttribute.TEX_U, VertexAttribute.NORMAL, VertexAttribute.TEX_V, VertexAttribute.COLOUR)
		val newVertices = vertices.map { it.convert(newLayout) }.toTypedArray()
		val mesh = VulkanModelMesh(vulkan, id, newVertices, indices.toBuffer(), material)
		vulkan.deletionQueue.push(mesh)
		return mesh
	}
}