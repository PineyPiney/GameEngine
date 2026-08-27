package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlArrayMesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlIndexedMesh
import com.pineypiney.game_engine.resources.models.OpenGlModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderModule
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.opengl.OpenGlComputeShader
import com.pineypiney.game_engine.resources.shaders.opengl.OpenGlRenderShader
import com.pineypiney.game_engine.resources.shaders.opengl.SubShader
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture3D
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.toString
import java.nio.ByteBuffer

class OpenGlResourceFactory : ResourceFactory() {

	override fun createShaderModule(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String): SubShader {
		if ((stage == ShaderStage.TESS_CTRL || stage == ShaderStage.TESS_EVAL) && !GLFunc.versionAtLeast(4, 1)) {
			if (!ShaderLoader.warnedTess) {
				GameEngineI.logger.warn(
					"Tried to create Tesselation Shader, which requires OpenGL 4.1 or higher, but created OpenGL Instance is version ${
						GLFunc.version.toString(
							".",
							Int::toString
						)
					}"
				)
				ShaderLoader.warnedTess = true
			}
			return SubShader("Error", stage, 0, emptyMap())
		}
		if (stage == ShaderStage.COMPUTE && !GLFunc.versionAtLeast(4, 3)) {
			if (!ShaderLoader.warnedCompute) {
				GameEngineI.logger.warn("Tried to create Compute Shader, which requires OpenGL 4.3 or higher, but created OpenGL Instance is version ${GLFunc.version.toString(".", Int::toString)}")
				ShaderLoader.warnedCompute = true
			}
			return SubShader("Error", stage, 0, emptyMap())
		}

		return ShaderLoader.INSTANCE.loadShaderModuleOpenGl(fileName.removeSuffix(".$suf"), code, stage)
	}

	override fun createRenderShader(vertex: ShaderModule, fragment: ShaderModule, stages: List<ShaderModule>, parameters: RenderShaderParameters, deletionQueue: DeletionQueue): OpenGlRenderShader {
		return ShaderLoader.generateGraphicsShaderOpenGl(vertex as SubShader, fragment as SubShader, stages.filterIsInstance<SubShader>(), parameters)
	}

	override fun createComputeShader(compute: ShaderModule): OpenGlComputeShader {
		return ShaderLoader.generateComputeShaderOpenGl(compute as SubShader)
	}

	override fun nullTexture2D(): OpenGlTexture2D = OpenGlTexture2D("Null", 0)
	override fun nullTexture3D(): OpenGlTexture3D = OpenGlTexture3D("Null", 0)

	override fun createTexture2D(name: String, width: Int, height: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): OpenGlTexture2D {
		val pointer = OpenGlTexture2D.createPointer(data, dataFormat, width, height, format.opengl, params)
		return OpenGlTexture2D(name, pointer, params.target)
	}

	override fun createTexture3D(name: String, width: Int, height: Int, depth: Int, format: TextureFormat, dataFormat: TextureFormat, data: ByteBuffer?, params: TextureParameters): OpenGlTexture3D {
		val pointer = OpenGlTexture3D.createPointer(data, dataFormat, width, height, depth, format.opengl, params)
		return OpenGlTexture3D(name, pointer, params.target)
	}

	override fun createArrayMesh(name: String, vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): OpenGlArrayMesh {
		return OpenGlArrayMesh(vertices, attributes)
	}

	override fun createIndexedMesh(name: String, vertices: ByteBuffer, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): OpenGlIndexedMesh {
		return OpenGlIndexedMesh(vertices, attributes, indices)
	}

	override fun createIndexedMesh(name: String, vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): OpenGlIndexedMesh {
		return OpenGlIndexedMesh(vertices, attributes, indices)
	}

	override fun createModelMesh(id: String, vertices: Array<out MeshVertex>, indices: IntArray, alpha: Float, order: Int, material: ModelMaterial): OpenGlModelMesh {
		return OpenGlModelMesh(id, vertices, indices, alpha, order, material)
	}
}