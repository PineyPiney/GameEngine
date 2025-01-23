package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.ObjectCollectionLayer
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class EditorRenderer(override val window: WindowI) : BufferedGameRenderer<EditorScreen>() {

	override val view = I
	override val projection = I
	override val guiProjection = I

	override val camera: OrthographicCamera = OrthographicCamera(window)

	override fun init() {
		super.init()

		GLFunc.blend = true
		GLFunc.blendFunc = Vec2i(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA)
		GLFunc.clearColour = Vec4(1f)
	}

	override fun render(game: EditorScreen, tickDelta: Double) {
		camera.getView(view)
		camera.getProjection(projection)

		clearFrameBuffer()
		GLFunc.viewportO = Vec2i(buffer.width, buffer.height)

		for((_, layer) in game.sceneObjects.map) renderLayer(layer, tickDelta)
		renderLayer(game.gameObjects.map[1]!!, tickDelta)

		// This draws the buffer onto the screen
		FrameBuffer.unbind()
		GLFunc.viewportO = window.framebufferSize
		clear()
		screenShader.setUp(screenUniforms, this)
		buffer.draw()
		GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
	}

	fun renderLayer(objects: ObjectCollectionLayer, tickDelta: Double){
		for(o in objects.flatMap { it.catchRenderingComponents() }){
			val renderedComponents = o.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
			if (renderedComponents.isNotEmpty()) {
				for (c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
				for (c in renderedComponents) c.render(this, tickDelta)
			}
			else for(c in o.components.filterIsInstance<PreRenderComponent>()){
				if(!c.whenVisible) c.preRender(this, tickDelta)
			}
		}
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		super.updateAspectRatio(window, objects)
		val w = window.aspectRatio
		glm.ortho(-w, w, -1f, 1f, guiProjection)
	}
}