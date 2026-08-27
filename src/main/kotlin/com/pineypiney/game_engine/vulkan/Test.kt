package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.Movement3D
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.vulkan.VulkanGameRenderer
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.text.Text
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowGameLogic
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class Logic(override val gameEngine: VulkanGameEngine<Logic>) : WindowGameLogic() {
	override val renderer: VulkanGameRenderer<Logic, PerspectiveCamera> = VulkanGameRenderer(window, gameEngine.vulkanManager, PerspectiveCamera(window))

	val movement = Movement3D.default(window, renderer.camera as Camera, 1f)
	val arrow = MeshedTextureComponent(
		GameObject("Arrow"),
		TextureLoader.Companion[ResourceKey("broke")],
		MeshedTextureComponent.default3DShader,
		ModelLoader.Companion[ResourceKey("gltf/Arrow")].meshes.first()
	).applied()
	val quad = MeshedTextureComponent(GameObject("Quad"), Texture2D.missing, mesh = Mesh.centerSquareShape).applied()
	val text = Text.makeMenuText("Test Text", Vec4(1f), 20)
	val fps = FPSCounter.createCounterWithText(
		GameObject("FPS Counter", 1), 1.0, "FPS: $",
		Text.Params(Vec4(1f), 20, Text.ALIGN_TOP_RIGHT)
	)

	override fun addObjects() {
		quad.parent.translate(Vec3(2f, 0f, 0f))
		add(movement.parent, arrow.parent, quad.parent)

		text.pixel(0, -32, 200, 24, -1f, 1f)
		fps.pixel(-200, -24, 200, 24, 1f, 1f)
		add(text, fps)
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT
		else if (action == 0) {
			if (state.triggers(InputState(GLFW.GLFW_KEY_ESCAPE))) {
				window.shouldClose = true
				return InteractorComponent.INTERRUPT
			} else if (state.triggers(InputState('M'))) {
				movement.look = !movement.look
				return InteractorComponent.INTERRUPT
			}
		}
		return action
	}
}

fun main() {
	LibrarySetUp.initGLFW()

	val hints = Window.defaultHints + (GLFW.GLFW_CLIENT_API to GLFW.GLFW_NO_API)
	val window = object : Window("Vulkan Window", 1280, 720, false, false, hints) {
		override val input: Inputs = DefaultInput(this)
		override fun init() {
			// Make the window visible
			GLFW.glfwShowWindow(windowHandle)
			super.init()
		}
	}
	window.init()

	val engine = object : VulkanGameEngine<Logic>(window, VulkanManager()) {
		override fun setLogic() {
			activeScreen = Logic(this)
		}
	}
	engine.run()
}