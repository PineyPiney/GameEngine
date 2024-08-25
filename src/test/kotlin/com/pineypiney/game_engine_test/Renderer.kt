package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponent
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11C.*

class Renderer<R: CameraI>(override val window: WindowI, override val camera: R): BufferedGameRenderer<WindowGameLogic>() {

	override val view = I
	override val projection = I
	override val guiProjection = I

	override fun init() {
		super.init()

		GLFunc.blend = true
		GLFunc.blendFunc = Vec2i(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		screenUniforms.setIntUniform("effects") { 0 }
	}

	override fun render(game: WindowGameLogic, tickDelta: Double) {

		camera.getView(view)
		camera.getProjection(projection)

		clearFrameBuffer()
		GLFunc.depthTest = true
		for(o in game.gameObjects.gameItems.flatMap { it.allActiveDescendants() }) {
			val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
			if(renderedComponents.isNotEmpty()){
				for(c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(tickDelta)
				for(c in renderedComponents) c.render(this, tickDelta)
			}
		}

		GLFunc.depthTest = false
		for(o in game.gameObjects.guiItems.flatMap { it.allActiveDescendants() }) {
			val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
			if(renderedComponents.isNotEmpty()){
				for(c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(tickDelta)
				for(c in renderedComponents) c.render(this, tickDelta)
			}
		}

		// This draws the buffer onto the screen
		FrameBuffer.unbind()
		clear()
		screenShader.setUp(screenUniforms, this)
		buffer.draw()
		glClear(GL_DEPTH_BUFFER_BIT)
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		super.updateAspectRatio(window, objects)
		val w = window.aspectRatio
		glm.ortho(-w, w, -1f, 1f, guiProjection)
	}
}