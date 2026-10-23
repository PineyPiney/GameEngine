package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import com.pineypiney.game_engine.resources.shaders.ComputeShader
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderModule
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.ResourceKey
import kool.free
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer


abstract class ResourceFactory {

	abstract fun createShaderModule(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String): ShaderModule
	abstract fun createRenderShader(
		id: ResourceKey,
		vertex: ShaderModule,
		fragment: ShaderModule,
		stages: List<ShaderModule>,
		parameters: RenderShaderParameters
	): RenderShader

	abstract fun createComputeShader(compute: ShaderModule): ComputeShader

	abstract fun nullTexture2D(): Texture2D
	abstract fun nullTexture3D(): Texture3D
	abstract fun createTexture2D(name: String, width: Int, height: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): Texture2D
	abstract fun createTexture3D(name: String, width: Int, height: Int, depth: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): Texture3D

	abstract fun createArrayMesh(name: String, vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createIndexedMesh(name: String, vertices: ByteBuffer, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createIndexedMesh(name: String, vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createModelMesh(id: String, vertices: List<MeshVertex>, indices: IntArray, material: ModelMaterial = PhongMaterial(id, emptyMap())): ModelMesh

	fun loadTexture2DFromFile(fileName: String, stream: InputStream, parameters: TextureParameters = TextureParameters()): Texture2D {

		val buffer = ResourcesLoader.ioResourceToByteBuffer(stream)
		if (!buffer.hasRemaining()) {
			GameEngineI.warn("Buffer for texture $fileName is empty")
			return Texture2D.missing
		}

		val (fileData, vec) = TextureLoader.loadTextureData(buffer, parameters.flip, 0)
		buffer.free()

		if (fileData != null) {
			val textureData: ByteBuffer
			var format = TextureLoader.channelsToStbiFormat(vec.z)
			if(!format.supported){
				when(format){
					TextureFormat.RG8 -> {
						val pixels = fileData.remaining() / 2
						textureData = ByteBuffer.allocateDirect(4 * pixels)
						format = TextureFormat.RGBA8
						repeat(pixels){ i ->
							textureData.put(i * 4, fileData, i * 2, 2)
						}
					}
					TextureFormat.RGB8 -> {
						val pixels = fileData.remaining() / 3
						textureData = ByteBuffer.allocateDirect(4 * pixels)
						format = TextureFormat.RGBA8
						repeat(pixels){ i ->
							textureData.put(i * 4, fileData, i * 3, 3)
						}
					}
					else -> {
						STBImage.stbi_image_free(fileData)
						return Texture2D.missing
					}
				}
			}
			else textureData = fileData
			val texture = createTexture2D(fileName.substringBeforeLast('.'), vec.x, vec.y, format, format, textureData, parameters)
			STBImage.stbi_image_free(fileData)
			return texture
		}
		GameEngineI.warn("\nSTB failed to load texture $fileName")
		return Texture2D.missing
	}

	companion object {
		val INSTANCE: ResourceFactory get() = ResourcesLoader.INSTANCE.factory
	}
}