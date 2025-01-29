package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class EditorRenderer(window: WindowI) : DefaultWindowRenderer<EditorScreen, OrthographicCamera>(window, OrthographicCamera(window)) {

	override val view = I
	override val projection = I
	override val guiProjection = I

	override fun init() {
		super.init()

		GLFunc.clearColour = Vec4(1f)
		GLFunc.multiSample = true
	}

	override fun render(game: EditorScreen, tickDelta: Double) {
		camera.getView(view)
		camera.getProjection(projection)

		clearFrameBuffer()

		for((_, layer) in game.sceneObjects.map) renderLayer(layer, tickDelta)
		renderLayer(1, game, tickDelta)

		// This draws the buffer onto the screen
		FrameBuffer.unbind()
		clear()
		screenShader.setUp(screenUniforms, this)
		buffer.draw()
		GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
	}
}