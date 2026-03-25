package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.LightComponent
import com.pineypiney.game_engine.objects.components.Movement3D
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.rendering.ModelRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.objects.components.widgets.slider.ActionSliderComponent
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.vectorToEuler
import com.pineypiney.game_engine.util.text.Text
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.quat.Quat
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import kotlin.math.PI

class Game3D(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<Game3D, PerspectiveCamera>(window, PerspectiveCamera(window))
	private val camera get() = renderer.camera

	private val pressedKeys = mutableSetOf<Short>()

	private var updateRay = true

	private val movementController = Movement3D.default(window, camera, 10f)

	private val indexSlider = ActionSliderComponent.createFloatSliderAt("Index Slider", Vec2(-1f), Vec2(1f, .3f), .98f, 1f, 1f) {
		ModelMesh.indicesMult = it.value
	}

	private val crosshair = GameObject.simpleRenderedGameObject("Crosshair", ShaderLoader[ResourceKey("vertex/crosshair"), ResourceKey("fragment/crosshair")], Vec3(0f), Vec3(Vec2(.2f), 1f)) {}

	private val cursorRay = GameObject.simpleModelledGameObject("Cursor Ray", ModelLoader[ResourceKey("gltf/Arrow")], ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/plain")])

	private val object3D = GameObject.simpleTextureGameObject("Broke Cube", TextureLoader[ResourceKey("broke")], Mesh.centerCubeShape, MeshedTextureComponent.default3DShader)
		.apply { rotation = Quat(Vec3(0.4, PI / 4, 1.2)) }

	var blockHover = false
	private val block = GameObject.simpleRenderedGameObject("Lit Cube", ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/pbr_lit_model")], mesh = Mesh.centerCubeShape) {
		shader.setLightUniforms(parent)
		shader.setVec4("material.baseColourFactor", Vec4(1.0))
		shader.setFloat("material.roughnessFactor", .5f)
		shader.setFloat("material.metallicFactor", .5f)
	}

	private val doughnut = GameObject.simpleModelledGameObject("Broke Model", ModelLoader[ResourceKey("broke")], ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/pbr_lit_model")])
		.apply { translate(Vec3(0f, 2f, 0f)) }
	private val gltf = GameObject.simpleModelledGameObject("Heart", ModelLoader[ResourceKey("gltf/SketchUp")], ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/pbr_lit_model")])
		.apply { translate(Vec3(2f, 2f, 0f)); resize(Vec3(.002f)) }
	private val voxel =
		GameObject.simpleModelledGameObject("Voxel", ModelLoader[ResourceKey("voxel/Mecha01")], ColourRendererComponent.vertexColours).apply { translate(Vec3(0f, -4f, 0f)); scale(Vec3(.1f)) }

	val sun = GameObject.simpleLightObject(DirectionalLight(Vec3(.1f, -.9f, .1f)))
	val light = GameObject.simpleLightObject(PointLight())
	val torch = GameObject.simpleLightObject(SpotLight(camera.cameraFront), false)

	val fpsText = FPSCounter.createCounterWithText(GameObject("FPS Text", 1).apply { pixel(Vec2i(-200, -100), Vec2i(200, 100), Vec2(1f)) }, 2.0, "FPS: $", Text.Params(Vec4(0f, 0f, 0f, 1f), 32, Text.ALIGN_TOP_RIGHT))

	override fun init() {
		super.init()
		//GLFunc.cullFace = true
		glfwSetInputMode(window.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
		gltf.addChild(CollisionBox3DRenderer.create(gltf).apply { init() })
	}

	override fun addObjects() {
		add(movementController.parent)
		add(object3D.apply { translate(Vec3(-2, 0, 0)) })
		add(block)
		add(crosshair)
		add(cursorRay)
		add(doughnut.apply { getComponent<ModelRendererComponent>()?.setAnimation("TorusAction") })
		add(gltf)
		add(voxel)
		add(light, torch, sun.apply { position = Vec3(0f, 900f, 0f); scale = Vec3(50f) })
		add(indexSlider.parent)
		add(fpsText)
	}

	override fun render(tickDelta: Double) {

		renderer.render(this, tickDelta)

		val speed = 10 * Timer.frameDelta
		val travel = Vec3()

		val forward = camera.cameraUp cross camera.cameraRight
		if(pressedKeys.contains('W'.s)) travel += forward
		if(pressedKeys.contains('S'.s)) travel -= forward
		if(pressedKeys.contains('A'.s)) travel -= camera.cameraRight
		if(pressedKeys.contains('D'.s)) travel += camera.cameraRight
		if(pressedKeys.contains(' '.s)) travel += camera.cameraUp
		if(pressedKeys.contains(GLFW_KEY_LEFT_CONTROL.s)) travel -= camera.cameraUp

		if(travel != Vec3(0)){
			camera.translate(travel * speed)
			torch.position = camera.cameraPos
		}

		light.position = Vec2.fromAngle(Timer.frameTime.mod(PI * 2).toFloat() * 2f, 10f).run { Vec3(x, 2f, y) }

		object3D.rotate(Vec3(0.5, 1, 1.5) * Timer.frameDelta)
		val ray = camera.getRay(input.mouse.lastPos.screenSpace)

		val shape = Cuboid(Vec3(0f), Quat.identity, Vec3(1f)) transformedBy object3D.worldModel
		val hit = shape.intersectedBy(ray).isEmpty()
		object3D.getComponent<MeshedTextureComponent>()!!.texture = if(hit) Texture.broke else TextureLoader[ResourceKey("snake/snake_0")]

		if(updateRay){
			cursorRay.position = ray.rayOrigin + (ray.direction * cursorRay.scale.x * 1.5f)
			val (p, y) = vectorToEuler(ray.direction)
			cursorRay.rotation = Quat(Vec3(-p, -y, 0f))
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte) {
		super.onPrimary(window, action, mods)
		if(action == 1) updateRay = !updateRay
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
				'Z' -> window.size = Vec2i(window.videoMode.width(), window.videoMode.height())
				'X' -> glfwSetInputMode(window.windowHandle, GLFW_CURSOR, when(glfwGetInputMode(window.windowHandle, GLFW_CURSOR)){ GLFW_CURSOR_NORMAL -> GLFW_CURSOR_DISABLED; else -> GLFW_CURSOR_NORMAL })
				'C' -> input.mouse.setCursorAt(Vec2(0.75))
				'V' -> window.vSync = !window.vSync
				'M' -> toggleMouse()
				'T' -> torch.getComponent<LightComponent>()?.toggle()
				'L' -> {
					camera.setPos(Vec3(0f, 0f, -5f))
					movementController.resetLook()
					torch.position = camera.cameraPos
					(torch.getComponent<LightComponent>()?.light as? SpotLight)?.direction = camera.cameraFront
				}
			}
		}


		if(action == 0) pressedKeys.remove(state.key)
		else pressedKeys.add(state.key)
		return action
	}

	override fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
		super.onCursorMove(cursorPos, cursorDelta)

		if (movementController.look) (torch.getComponent<LightComponent>()?.light as? SpotLight)?.direction = camera.cameraFront

		val ray = camera.getRay(cursorPos.screenSpace)
		blockHover = (Cuboid(Vec3(0f), Quat.identity, Vec3(1f)) transformedBy block.worldModel).intersectedBy(ray).isNotEmpty()
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}

	private fun toggleMouse(){
		movementController.look = !movementController.look
		glfwSetInputMode(window.windowHandle, GLFW_CURSOR, if (movementController.look) GLFW_CURSOR_DISABLED else GLFW_CURSOR_CAPTURED)
	}
}