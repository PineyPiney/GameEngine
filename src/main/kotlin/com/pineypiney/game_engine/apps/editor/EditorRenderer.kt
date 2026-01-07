package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.Java.Companion.glm
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class EditorRenderer(window: WindowI, val settings: EditorSettings, val sort: GameObject.() -> Float, val depth: Boolean) : DefaultWindowRenderer<EditorScreen, OrthographicCamera>(window, OrthographicCamera(window)) {

	override val view = I
	override val projection = I
	override val guiProjection = I

	var backgroundColour = Colour(0, 0, 0)
	val sceneFramebuffer = Framebuffer(0, 0)

	var sceneMesh: Mesh = Mesh.textureQuad(Vec2(-1f), Vec2(1f))

	override fun init() {
		framebuffer.format = GL11C.GL_RGBA
		framebuffer.internalFormat = GL11C.GL_RGBA
		super.init()
		GLFunc.multiSample = true
	}

	override fun render(game: EditorScreen, tickDelta: Double) {
		val sceneBox = game.getSceneBox()
		camera.getView(view)
		camera.getProjection(projection)

		GLFunc.clearColour = backgroundColour.rgbaValue
		GLFunc.depthTest = depth

		viewportSize = sceneBox.size
		aspectRatio = sceneBox.aspectRatio
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)
		clearFrameBuffer(sceneFramebuffer)

		for((_, layer) in game.sceneObjects.map) renderLayer(layer, tickDelta, sceneFramebuffer.FBO, sort)
		GLFunc.depthTest = false
		game.transformer?.let {
			for(obj in it.catchRenderingComponents()) renderObject(obj, tickDelta, sceneFramebuffer.FBO)
		}

		GLFunc.clearColour = Vec4(0f)

		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio
//		camera.updateAspectRatio(aspectRatio)
//		camera.getProjection(projection)
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)
		clearFrameBuffer()

		renderLayer(1, game, tickDelta, framebuffer){ transformComponent.worldPosition.z}

		// This draws the buffer onto the screen
		Framebuffer.unbind()
		clear()
		screenShader.setUp(screenUniforms, this)
		sceneFramebuffer.draw(sceneMesh)
		framebuffer.draw()
		GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
	}

	fun createSceneBufferMesh(): Mesh {
		return Mesh.textureQuad(Vec2((settings.objectBrowserWidth * 2f / viewportSize.x) - 1f, (settings.fileBrowserHeight * 2f / viewportSize.y) - 1f), Vec2(1f - (settings.componentBrowserWidth * 2f / viewportSize.x), 1f))
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		camera.updateAspectRatio(sceneBox.aspectRatio)
		framebuffer.setSize(window.framebufferSize)
		sceneFramebuffer.setSize(sceneBox.size)

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