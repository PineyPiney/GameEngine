package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.textures.parameters.TextureUsage
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

open class EditorRenderer(
	window: WindowI,
	val settings: EditorSettings,
	val sort: GameObject.() -> Float,
	val depth: Boolean,
	apiFactory: (WindowRendererI<EditorScreen>) -> PresentingApi
) : GameRenderer<EditorScreen>(window, apiFactory) {

	override val camera = OrthographicCamera(window)

	val sceneFramebuffer: Framebuffer

	init {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		sceneFramebuffer = getRenderingApi().createFramebuffer(sceneBox.size, usage = setOf(TextureUsage.SAMPLER))
	}

	var backgroundColour = Colour(0, 255, 0)
	var sceneMesh: Mesh = createSceneBufferMesh()

	override fun init() {
		super.init()
		sceneFramebuffer.init()
	}

	override fun render(game: EditorScreen, tickDelta: Double) {

		camera.getView(view)
		camera.getProjection(projection)

		val api = getRenderingApi()
		if (!api.beginPresentation()) return
		val sceneBox = game.getSceneBox()

		// Prepare Scene Render

		viewportSize = sceneBox.size
		aspectRatio = sceneBox.aspectRatio
		api.updateGui(guiProjection, aspectRatio)

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
		api.updateGui(guiProjection, aspectRatio)

		api.bindFramebuffer(framebuffer)

		// Render GUI
		screenShader.setUp(screenUniforms, this)
		screenShader.setTexture("screenTexture", sceneFramebuffer.colour)
		screenShader.draw("vertexBuffer", sceneMesh, getRenderingApi())

		renderLayer(1, game, tickDelta) { transformComponent.worldPosition.z }

		api.endFramebuffer(framebuffer)

		api.copyFramebuffer(framebuffer, this)
		api.present()
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

	fun createSceneBufferMesh(): Mesh {
		return Mesh.textureQuad(
			ResourceFactory.INSTANCE, "Scene Buffer",
			Vec2((settings.objectBrowserWidth * 2f / viewportSize.x) - 1f, (settings.fileBrowserHeight * 2f / viewportSize.y) - 1f),
			Vec2(1f - (settings.componentBrowserWidth * 2f / viewportSize.x), 1f)
		)
	}

	override fun deleteFrameBuffers() {
		super.deleteFrameBuffers()
		sceneFramebuffer.delete()
	}

	override fun delete() {
		super.delete()
		sceneMesh.delete()
	}
}