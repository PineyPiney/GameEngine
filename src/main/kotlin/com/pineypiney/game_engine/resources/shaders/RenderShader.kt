package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.LightComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.serialisation.Codec.Companion.KEY
import kotlin.experimental.and

@Suppress("UNUSED")
interface RenderShader : Shader {

	val vertex: ShaderModule
	val fragment: ShaderModule
	val stages: List<ShaderModule>
	val parameters: RenderShaderParameters

	val screenMask: Byte
	val hasView get() = (screenMask and 1) > 0
	val hasProj get() = (screenMask and 2) > 0
	val hasGUI get() = (screenMask and 4) > 0
	val hasPort get() = (screenMask and 8) > 0
	val hasPos get() = (screenMask and 0x10) > 0

	val lightMask: Byte
	val hasDirL get() = (lightMask and 1) > 0
	val hasPointL get() = (lightMask and 2) > 0
	val hasSpotL get() = (lightMask and 4) > 0

	fun draw(meshName: String, mesh: Mesh, renderer: RendererI) =
		draw(meshName, mesh, renderer.getRenderingApi())

	fun draw(meshName: String, mesh: Mesh, api: RenderingApi)

	override fun getModule(stage: ShaderStage): ShaderModule? {
		return when (stage) {
			ShaderStage.VERTEX -> vertex
			ShaderStage.FRAGMENT -> fragment
			else -> getSubShader(stage)
		}
	}

	fun setRendererDefaults(uniforms: Uniforms){
		if (hasView) uniforms.setMat4UniformR("view", RendererI::view)
		if (hasProj) uniforms.setMat4UniformR("projection", RendererI::projection)
		if (hasGUI) uniforms.setMat4UniformR("guiProjection", RendererI::guiProjection)
		if (hasPort) uniforms.setVec2iUniformR("viewport", RendererI::viewportSize)
		if (hasPos) uniforms.setVec3UniformR("viewPos", RendererI::viewPos)
	}

	fun setLightUniforms(obj: GameObject, lights: List<LightComponent> = obj.objects?.getAllComponents()?.filterIsInstance<LightComponent>()?.filter { it.light.on } ?: emptyList()) {
		if(lights.isEmpty()) return

		val dirLight = lights.firstOrNull { it.light is DirectionalLight }
		if(dirLight == null) Light.setShaderUniformsOff(this, "dirLight")
		else dirLight.setShaderUniforms(this, "dirLight")

		val pointLights = lights.filter { it.light is PointLight }.sortedByDescending { (it.parent.position - obj.position).length() / (it.light as PointLight).linear }
		for (l in 0..<4) {
			val name = "pointLights[$l]"
			if (l < pointLights.size) pointLights[l].setShaderUniforms(this, name)
			else Light.setShaderUniformsOff(this, name)
		}

		val spotLight = lights.firstOrNull { it.light is SpotLight }
		if(spotLight == null) Light.setShaderUniformsOff(this, "spotlight")
		else spotLight.setShaderUniforms(this, "spotlight")
	}

	fun getSubShader(stage: ShaderStage) = stages.firstOrNull { it.getStage() == stage }

	fun withParameters(parameters: RenderShaderParameters, deletionQueue: DeletionQueue = DeletionQueue.GLOBAL) =
		ResourceFactory.INSTANCE.createRenderShader(vertex, fragment, stages, parameters, deletionQueue)

	companion object {

		val OPTIONAL_STAGES = setOf(ShaderStage.TESS_CTRL, ShaderStage.TESS_EVAL, ShaderStage.GEOMETRY)

		lateinit var missing: RenderShader

		val CODEC = Codec.map(
			KEY.field("v") { it: RenderShader -> ResourceKey(it.vertex.getName()) },
			KEY.field("f") { it: RenderShader -> ResourceKey(it.fragment.getName()) },
			KEY.opnull().field("tc") { it: RenderShader -> it.getSubShader(ShaderStage.TESS_CTRL)?.let { ResourceKey(it.getName()) } },
			KEY.opnull().field("te") { it: RenderShader -> it.getSubShader(ShaderStage.TESS_EVAL)?.let { ResourceKey(it.getName()) } },
			KEY.opnull().field("g") { it: RenderShader -> it.getSubShader(ShaderStage.GEOMETRY)?.let { ResourceKey(it.getName()) } },
			RenderShaderParameters.CODEC.optional(RenderShaderParameters()).field(RenderShader::parameters, "params")
		) { v, f, tc, te, g, p -> ShaderLoader[v, f, listOfNotNull(tc, te, g), p] }

		fun initDefaultShader(loader: ResourcesLoader) {

			val version = if (GLFunc.isLoaded) "330"
			else "430"

			val vS = """
				#version $version core
				
				#ifdef OPENGL
				layout (location = 0) in vec2 aPos;
				#endif
				
				#ifdef VULKAN
				#extension GL_EXT_buffer_reference : require
				struct Vertex {
					vec2 position;
				};

				layout(buffer_reference, std430) readonly buffer VertexBuffer{
					Vertex vertices[];
				};

				//push constants block
				layout(push_constant) uniform constants
				{
					mat4 model;
					VertexBuffer vertexBuffer;
				};
				#endif
				
				layout (location = 0) out vec3 outColor;
		 
				void main(){
					#ifdef VULKAN
					vec2 aPos = vertexBuffer.vertices[gl_VertexID].position;
					#endif
					gl_Position = vec4(aPos, 0.0, 1.0);
				}
			""".trimIndent()

			val fS = """
				#version 330 core
				
				#ifdef OPENGL
				out vec4 FragColour;
				#endif
				
				#ifdef VULKAN
				layout (location = 0) out vec4 FragColour;
				#endif
				
				void main(){
					FragColour = vec4(1.0, 1.0, 1.0, 1.0);
				}
			""".trimIndent()


			val vertex = loader.factory.createShaderModule(loader, "missing_vertex", "", ShaderStage.VERTEX, vS)
			val fragment = loader.factory.createShaderModule(loader, "missing_fragment", "", ShaderStage.FRAGMENT, fS)
			missing = loader.factory.createRenderShader(vertex, fragment, emptyList(), RenderShaderParameters())
		}
	}
}