package com.pineypiney.game_engine.apps.editor.renderer

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.vulkan.VulkanGameRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureUsage
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowI

open class VulkanEditorRenderer(
	window: WindowI,
	override val settings: EditorSettings,
	override val sort: GameObject.() -> Float,
	override val depth: Boolean
) : VulkanGameRenderer<EditorScreen, OrthographicCamera>(window, OrthographicCamera(window)), EditorRenderer {

	val sceneFramebuffer: Framebuffer

	init {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		sceneFramebuffer = getRenderingApi().createFramebuffer(sceneBox.size.x, sceneBox.size.y, TextureFormat.RGBA8, usage = setOf(TextureUsage.SAMPLER))
	}

	override var backgroundColour = Colour(0, 255, 0)
	var sceneMesh: Mesh = createSceneBufferMesh()

	override fun renderWithFramebuffer(game: EditorScreen, tickDelta: Double) {

		val api = getRenderingApi()
		val sceneBox = game.getSceneBox()

		// Prepare Scene Render

		viewportSize = sceneBox.size
		aspectRatio = sceneBox.aspectRatio
		updateGui()

		api.bindFramebuffer(sceneFramebuffer, backgroundColour.rgbaVec)

		// Render Scene

		for ((_, layer) in game.sceneObjects.map) renderLayer(layer, tickDelta, sort)
		game.transformer?.let {
			for (obj in it.catchRenderingComponents()) renderObject(obj, tickDelta)
		}

		api.endFramebuffer(sceneFramebuffer)


		// Prepare GUI Render

		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio
		updateGui()

		api.bindFramebuffer(framebuffer)

		// Render GUI
		screenShader.setUp(screenUniforms, this)
		screenShader.setTexture("screenTexture", sceneFramebuffer.colour)
		screenShader.draw("vertexBuffer", sceneMesh, getRenderingApi())

		renderLayer(1, game, tickDelta, sort)

		api.endFramebuffer(framebuffer)
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		camera.updateAspectRatio(sceneBox.aspectRatio)

		// This is updated here for TransformComponent updates
		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio

		framebuffer.setSize(window.size)
		sceneFramebuffer.setSize(sceneBox.size)

		sceneMesh.delete()
		sceneMesh = createSceneBufferMesh()
	}

	override fun delete() {
		super.delete()
		sceneFramebuffer.delete()
		sceneMesh.delete()
	}

	companion object {
		val screenShader = ShaderLoader[ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer")]
		val screenUniforms = screenShader.compileUniforms()
	}
}