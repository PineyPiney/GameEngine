package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import kool.free
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer


abstract class ResourceFactory {

	init {
		INSTANCE = this
	}

	abstract fun nullTexture2D(): Texture2D
	abstract fun nullTexture3D(): Texture3D

	abstract fun createSubShader(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String)
	abstract fun createShader()
	abstract fun createTexture2D(name: String, width: Int, height: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): Texture2D
	abstract fun createTexture3D(name: String, width: Int, height: Int, depth: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): Texture3D
	abstract fun createArrayMesh(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createIndexedMesh(vertices: ByteBuffer, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createIndexedMesh(vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh
	abstract fun createModelMesh(id: String, vertices: Array<out MeshVertex>, indices: IntArray, alpha: Float = 1f, order: Int = 0, material: ModelMaterial = PhongMaterial(id, emptyMap())): ModelMesh

	fun loadTexture2DFromFile(fileName: String, stream: InputStream, parameters: TextureParameters = TextureParameters()): Texture2D {

		val buffer = ResourcesLoader.ioResourceToByteBuffer(stream)
		if (!buffer.hasRemaining()) {
			GameEngineI.warn("Buffer for texture $fileName is empty")
			return Texture2D.missing
		}

		GameEngineI.info("Loading texture $fileName")
		val (data, vec) = TextureLoader.loadTextureData(buffer, parameters.flip, 0)
		buffer.free()

		if (data != null) {
			val format = TextureLoader.channelsToStbiFormat(vec.z)
			val texture = createTexture2D(fileName.substringBeforeLast('.'), vec.x, vec.y, format, format, data, parameters)
			STBImage.stbi_image_free(data)
			return texture
		}
		GameEngineI.warn("\nSTB failed to load texture $fileName")
		return Texture2D.missing
	}

	companion object {
		lateinit var INSTANCE: ResourceFactory; private set
	}
}