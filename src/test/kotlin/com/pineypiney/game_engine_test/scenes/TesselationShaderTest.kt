package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.Movement3D
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.OpenGlGameRenderer.Companion.screenShader
import com.pineypiney.game_engine.rendering.OpenGlGameRenderer.Companion.screenUniforms
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.meshes.TessellatedMesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.text.Text
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11C

@Suppress("UNUSED")
class TesselationShaderTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<TesselationShaderTest, PerspectiveCamera>(window, PerspectiveCamera(window))

	init {
		GLFunc.patchVertices = 4
	}

	init {
		println("Max Patch Vertices: ${GLFunc.maxPatchVertices}")
		println("Max Tessellation Level: ${GLFunc.maxTessLevel}")
	}

	val heightMap = TextureLoader[ResourceKey("swiss_height_map")]
	val shader = ShaderLoader.get(ResourceKey("vertex/pass_through"), ResourceKey("fragment/texture"), ResourceKey("tessellation/dynamic_lod"), tessEvalKey = ResourceKey("tessellation/random_colour"))
	val uniformValues = mutableMapOf<String, Any>()

	val obj = GameObject.simpleRenderedGameObject("Switzerland", shader, Vec3(0f), Vec3(1f), TessellatedMesh.generatePlane(heightMap.width * .1f, heightMap.height * .1f, 64), {
		uniforms.setFloatUniform("scale"){.2f}
		uniforms.setIntUniform("minTess"){ 4 }
		uniforms.setIntUniform("maxTess"){ minOf(64, GLFunc.maxTessLevel) }
	}){
		heightMap.bind()
	}

	var renderWireframe = false

	override fun addObjects() {
		add(obj)
		add(FPSCounter.createCounterWithText(GameObject("FPS Text", 1).apply { relative(Vec3(-1f, 0f, 0f), Vec2(1f))}, 2.0, "FPS: $", Text.Params(fontSize = 24)))
		add(Movement3D.default(window, renderer.camera, 10f).parent)
	}

	override fun render(tickDelta: Double) {

		renderer.camera.getView(renderer.view)
		renderer.camera.getProjection(renderer.projection)

		renderer.clearFrameBuffer()

		GLFunc.depthTest = true
		if(renderWireframe) GLFunc.polygonMode = GL11C.GL_LINE
		renderer.renderLayer(0, this, tickDelta, renderer.framebuffer)
		GLFunc.polygonMode = GL11C.GL_FILL

		GLFunc.depthTest = false
		renderer.renderLayer(1, this, tickDelta, renderer.framebuffer){ transformComponent.worldPosition.z }

		// This draws the buffer onto the screen
		Framebuffer.unbind()
		renderer.clear()
		screenShader.setUp(screenUniforms, renderer)
		renderer.framebuffer.draw(renderer.getRenderingApi())
		GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW.GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
				'Z' -> renderWireframe = !renderWireframe
			}
		}
		return action
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}
}