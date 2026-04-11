package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.*
import com.pineypiney.game_engine.resources.models.OpenGlModelMesh
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.textures.TextureI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.toString

class OpenGlResourceFactory : ResourceFactory() {

	override fun createSubShader(loader: ResourcesLoader, fileName: String, suf: String, stage: ShaderStage, code: String) {
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
			return
		}
		if (stage == ShaderStage.COMPUTE && !GLFunc.versionAtLeast(4, 3)) {
			if (!ShaderLoader.warnedCompute) {
				GameEngineI.logger.warn("Tried to create Compute Shader, which requires OpenGL 4.3 or higher, but created OpenGL Instance is version ${GLFunc.version.toString(".", Int::toString)}")
				ShaderLoader.warnedCompute = true
			}
			return
		}

		ShaderLoader.INSTANCE.loadShaderOpenGl(fileName.removeSuffix(".$suf"), code, stage.opengl)
	}

	override fun createShader() {
		TODO("Not yet implemented")
	}

	override fun createTexture(): TextureI {
		TODO("Not yet implemented")
	}

	override fun createArrayMesh(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		return ArrayMesh(vertices, attributes)
	}

	override fun createIndexedMesh(vertices: FloatArray, indices: IntArray, attributes: Map<VertexAttribute<*, *>, Long>): Mesh {
		return OpenGlIndexedMesh(vertices, attributes, indices)
	}

	override fun createModelMesh(id: String, vertices: Array<out MeshVertex>, indices: IntArray, alpha: Float, order: Int, material: ModelMaterial): OpenGlModelMesh {
		return OpenGlModelMesh(id, vertices, indices, alpha, order, material)
	}
}