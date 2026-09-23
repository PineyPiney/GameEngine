package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.Movement3D
import com.pineypiney.game_engine.rendering.DefaultWindowGameRenderer
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.parameters.CullMode
import com.pineypiney.game_engine.resources.shaders.parameters.InputTopology
import com.pineypiney.game_engine.resources.shaders.parameters.PolygonMode
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.text.Text
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

@Suppress("UNUSED")
class TesselationShaderTest(override val gameEngine: WindowedGameEngineI<*>, override val renderer: GameRenderer<TesselationShaderTest>) : WindowGameLogic() {

	@Suppress("UNCHECKED_CAST")
	val defaultRenderer get() = renderer as DefaultWindowGameRenderer<TesselationShaderTest, PerspectiveCamera>

	init {
		val api = renderer.getRenderingApi()
		println("Max Patch Vertices: ${api.getMaxTessellationPatchSize()}")
		println("Max Tessellation Level: ${api.getMaxTessellationLevel()}")
	}

	val heightMap = TextureLoader[ResourceKey("swiss_height_map")]
	val mesh = Mesh.tessellatedPlane(gameEngine.resourcesLoader.factory, "Switzerland Mesh", heightMap.width * .1f, heightMap.height * .1f, 64)

	val shader = ShaderLoader[ResourceKey("vertex/pass_through"), ResourceKey("fragment/texture"), listOf(
		ResourceKey("tessellation/dynamic_lod"),
		ResourceKey("tessellation/random_colour")
	), RenderShaderParameters(InputTopology.PATCHES, cullMode = CullMode.NONE, tesselation = 4)]
	val wireframeShader = shader.withParameters(shader.parameters.copy(fillMode = PolygonMode.LINE))
	val uniformValues = mutableMapOf<String, Any>()

	val obj = GameObject.simpleRenderedGameObject("Switzerland", shader, Vec3(0f), Vec3(1f), mesh, {
		uniforms.setFloatUniform("scale") { .2f }
		uniforms.setIntUniform("minTess") { 4 }
		uniforms.setIntUniformR("maxTess") { minOf(64, it.getRenderingApi().getMaxTessellationLevel()) }
		uniforms.setTextureUniform("heightMap", ::heightMap)
		uniforms.setTextureUniform("tex", ::heightMap)
	})
	val wireframeObj = GameObject.simpleRenderedGameObject("Wireframe Switzerland", wireframeShader, Vec3(0f), Vec3(1f), mesh, {
		uniforms.setFloatUniform("scale"){.2f}
		uniforms.setIntUniform("minTess"){ 4 }
		uniforms.setIntUniformR("maxTess") { minOf(64, it.getRenderingApi().getMaxTessellationLevel()) }
		uniforms.setTextureUniform("heightMap", ::heightMap)
		uniforms.setTextureUniform("tex", ::heightMap)
	})

	var renderWireframe = false

	override fun addObjects() {
		add(obj, wireframeObj)
		add(FPSCounter.createCounterWithText(GameObject("FPS Text", 1).apply { relative(Vec3(-1f, 0f, 0f), Vec2(1f))}, 2.0, "FPS: $", Text.Params(fontSize = 24)))
		add(Movement3D.default(window, renderer.camera, 10f).parent)
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW.GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
				'Z' -> {
					obj.active = renderWireframe
					renderWireframe = !renderWireframe
					wireframeObj.active = renderWireframe
				}
			}
		}
		return action
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		renderer.getRenderingApi().updateGui(renderer.guiProjection, window.aspectRatio)
	}

	override fun cleanUp() {
		mesh.delete()
		super.cleanUp()
	}
}