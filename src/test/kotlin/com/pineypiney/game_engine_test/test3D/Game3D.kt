package com.pineypiney.game_engine_test.test3D

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.LightComponent
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.rendering.ModelRendererComponent
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.vectorToEuler
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import com.pineypiney.game_engine_test.Renderer
import glm_.f
import glm_.quat.Quat
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW.*
import kotlin.math.PI

class Game3D(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = Renderer(window, PerspectiveCamera(window))
	private val camera get() = renderer.camera

	private val pressedKeys = mutableSetOf<Short>()
	private var moveMouse = false

	private var updateRay = true

	private val indexSlider = BasicActionSlider(Vec2(-1f), Vec2(1f, .3f), .98f,  1f, 1f){
		ModelMesh.indicesMult = it.value
	}

	private val crosshair = GameObject.simpleRenderedGameObject(ShaderLoader[ResourceKey("vertex/crosshair"), ResourceKey("fragment/crosshair")], Vec3(0f), Vec3(Vec2(.2f), 1f)){}

	private val cursorRay = GameObject.simpleModelledGameObject(ModelLoader[ResourceKey("gltf/arrow")], ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/plain")])

	private val object3D = GameObject.simpleTextureGameObject(TextureLoader[ResourceKey("broke")], Mesh.centerCubeShape, MeshedTextureComponent.default3DShader).apply{ rotation = Quat(Vec3(0.4, PI/4, 1.2)) }

	var blockHover = false
	private val block = GameObject.simpleRenderedGameObject(ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/lit")], shape = Mesh.centerCubeShape) {
		uniforms.setFloatUniform("ambient") { 0.1f }
		uniforms.setVec3Uniform("blockColour") { if (blockHover) Vec3(0.1, 0.9, 0.1) else Vec3(0.7f) }
		uniforms.setVec3Uniform("lightPosition") { Vec3(1, 5, 2) }
	}

	private val doughnut = GameObject.simpleModelledGameObject(ModelLoader[ResourceKey("broke")], ModelRendererComponent.defaultShader).apply { translate(Vec3(0f, 2f, 0f)) }
	private val gltf = GameObject.simpleModelledGameObject(ModelLoader[ResourceKey("gltf/Beating Heart 3")], ModelRendererComponent.defaultLitShader).apply { translate(Vec3(2f, 2f, 0f)); scale(Vec3(0.002f)) }

	val sun = GameObject.simpleLightObject(DirectionalLight(Vec3(.1f, -.9f, .1f)))
	val light = GameObject.simpleLightObject(PointLight())
	val torch = GameObject.simpleLightObject(SpotLight(camera.cameraFront), false)

	val fpsText = Text.makeMenuText("FPS: 0.0", fontSize = .1f, alignment = Text.ALIGN_TOP_RIGHT)

	override fun init() {
		super.init()
		glfwSetInputMode(window.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
		gltf.addChild(CollisionBox3DRenderer(gltf).apply { init() })
	}

	override fun addObjects() {
		add(object3D.apply { translate(Vec3(-2, 0, 0)) })
		add(block)
		add(crosshair)
		add(cursorRay)
		add(doughnut.apply { getComponent<ModelRendererComponent>()?.setAnimation("TorusAction") })
		add(gltf)
		add(light, torch, sun.apply { position = Vec3(0f, 900f, 0f); scale = Vec3(50f) })
		add(indexSlider)
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
		val ray = camera.getRay(input.mouse.screenSpaceCursor())

		val shape = object3D.getShape()
		val hit = shape.intersectedBy(ray).isEmpty()
		object3D.getComponent<MeshedTextureComponent>()!!.texture = if(hit) Texture.broke else TextureLoader[ResourceKey("snake/snake_0")]

		if(updateRay){
			cursorRay.position = ray.rayOrigin + (ray.direction * cursorRay.scale.x * 1.5f)
			val (p, y) = vectorToEuler(ray.direction)
			cursorRay.rotation = Quat(Vec3(p, y + (PI * 0.5).f, 0f))
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
					camera.cameraYaw = -90.0
					camera.cameraPitch = 0.0
					camera.updateCameraVectors()
					torch.position = camera.cameraPos
					(torch.getComponent<LightComponent>()?.light as? SpotLight)?.direction = camera.cameraFront
				}
			}
		}


		if(action == 0) pressedKeys.remove(state.key)
		else pressedKeys.add(state.key)
		return action
	}

	override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
		super.onCursorMove(cursorPos, cursorDelta)

		if(!moveMouse){
			input.mouse.setCursorAt(Vec2(0))
			camera.cameraYaw += cursorDelta.x * 20
			camera.cameraPitch = (camera.cameraPitch + cursorDelta.y * 20).coerceIn(-89.99, 89.99)
			camera.updateCameraVectors()
			(torch.getComponent<LightComponent>()?.light as? SpotLight)?.direction = camera.cameraFront
		}

		val ray = camera.getRay(Vec2(cursorPos.x / window.aspectRatio, cursorPos.y))
		blockHover = block.getShape().intersectedBy(ray).isNotEmpty()
	}

	override fun updateAspectRatio(window: WindowI) {
		super.updateAspectRatio(window)
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}

	private fun toggleMouse(){
		moveMouse = !moveMouse
		glfwSetInputMode(window.windowHandle, GLFW_CURSOR, if(moveMouse) GLFW_CURSOR_CAPTURED else GLFW_CURSOR_DISABLED)
	}
}