package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.objects.util.meshes.SquareMesh
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.Java.Companion.glm
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class EditorRenderer(window: WindowI, val settings: EditorSettings) : DefaultWindowRenderer<EditorScreen, OrthographicCamera>(window, OrthographicCamera(window)) {

	override val view = I
	override val projection = I
	override val guiProjection = I

	var backgroundColour = Colour(0, 0, 0)
	val sceneBuffer = FrameBuffer(0, 0)

	var sceneMesh: Mesh = SquareMesh(Vec2(-1f), Vec2(1f))

	override fun init() {
		buffer.format = GL11C.GL_RGBA
		buffer.internalFormat = GL11C.GL_RGBA
		super.init()
		GLFunc.multiSample = true
	}

	override fun render(game: EditorScreen, tickDelta: Double) {
		camera.getView(view)
		camera.getProjection(projection)

		GLFunc.clearColour = backgroundColour.rgbaValue
		GLFunc.depthTest = true

		val sceneBox = game.getSceneBox()
		viewportSize = sceneBox.size
		aspectRatio = sceneBox.aspectRatio
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)
		clearFrameBuffer(sceneBuffer)

		for((_, layer) in game.sceneObjects.map) renderLayer(layer, tickDelta, sceneBuffer.FBO){ transformComponent.worldPosition.z}
		GLFunc.depthTest = false
		game.transformer?.let {
			for(obj in it.catchRenderingComponents()) renderObject(obj, tickDelta, sceneBuffer.FBO)
		}

		GLFunc.clearColour = Vec4(0f)

		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)
		clearFrameBuffer()

		renderLayer(1, game, tickDelta, buffer){ transformComponent.worldPosition.z}

		// This draws the buffer onto the screen
		FrameBuffer.unbind()
		clear()
		screenShader.setUp(screenUniforms, this)
		sceneBuffer.draw(sceneMesh)
		buffer.draw()
		GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
	}

	fun renderEditedScene(game: EditorScreen, tickDelta: Double){

	}

	fun createSceneBufferMesh(): Mesh {
		return SquareMesh(Vec2((settings.objectBrowserWidth * 2f / viewportSize.x) - 1f, (settings.fileBrowserHeight * 2f / viewportSize.y) - 1f), Vec2(1f - (settings.componentBrowserWidth * 2f / viewportSize.x), 1f))
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		camera.updateAspectRatio(sceneBox.aspectRatio)
		buffer.setSize(window.framebufferSize)
		sceneBuffer.setSize(sceneBox.size)

		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio

		sceneMesh.delete()
		sceneMesh = createSceneBufferMesh()
	}

	override fun deleteFrameBuffers() {
		super.deleteFrameBuffers()
		sceneBuffer.delete()
	}
}