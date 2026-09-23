package com.pineypiney.game_engine.apps.editor.renderer

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.DefaultWindowGameRenderer
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlIndexedMesh
import com.pineypiney.game_engine.rendering.opengl.OpenGlFramebuffer
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.WindowI
import glm_.Java.Companion.glm

class OpenGlEditorRenderer(
	window: WindowI,
	api: (GameRenderer<EditorScreen>) -> PresentingApi,
	override val settings: EditorSettings,
	override val sort: GameObject.() -> Float,
	override val depth: Boolean
) :
	DefaultWindowGameRenderer<EditorScreen, OrthographicCamera>(window, OrthographicCamera(window), api), EditorRenderer {

	override var backgroundColour = Colour(0, 255, 0)

	override val framebuffer: Framebuffer = getRenderingApi().createFramebuffer(window.width, window.height, TextureFormat.RGBA8)
	val sceneFramebuffer = OpenGlFramebuffer(0, 0)

	var sceneMesh: Mesh = OpenGlIndexedMesh.empty()

	override fun render(game: EditorScreen, tickDelta: Double) {

		// Prepare Scene Render
		camera.getView(view)
		camera.getProjection(projection)

		GLFunc.clearColour = backgroundColour.rgbaVec
		GLFunc.depthTest = depth

		val api = getRenderingApi()
		val sceneBox = game.getSceneBox()

		// Prepare Scene Render

		viewportSize = sceneBox.size
		aspectRatio = sceneBox.aspectRatio
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)

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
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)

		api.bindFramebuffer(framebuffer)

		// Render GUI
		VulkanEditorRenderer.screenShader.setUp(VulkanEditorRenderer.screenUniforms, this)
		VulkanEditorRenderer.screenShader.setTexture("screenTexture", sceneFramebuffer.colour)
		VulkanEditorRenderer.screenShader.draw("vertexBuffer", sceneMesh, getRenderingApi())

		renderLayer(1, game, tickDelta, sort)

		api.endFramebuffer(framebuffer)

		// Finish Render
		api.copyFramebuffer(framebuffer, this)
		api.present()
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		camera.updateAspectRatio(sceneBox.aspectRatio)
		framebuffer.setSize(window.framebufferSize)
		sceneFramebuffer.setSize(sceneBox.size)

		// This is updated here for TransformComponent updates
		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio

		sceneMesh.delete()
		sceneMesh = createSceneBufferMesh()
	}

	override fun deleteFrameBuffers() {
		super.deleteFrameBuffers()
		sceneFramebuffer.delete()
	}
}