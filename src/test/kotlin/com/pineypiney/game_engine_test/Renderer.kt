package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11C.*

class Renderer<R: CameraI>(override val window: WindowI, override val camera: R): BufferedGameRenderer<WindowGameLogic>() {

    var view = I
    var projection = I

    override fun init() {
        super.init()

        GLFunc.depthTest = true
        GLFunc.blend = true
        GLFunc.blendFunc = Vec2i(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        screenUniforms.setIntUniform("effects") { 0 }
    }

    override fun render(game: WindowGameLogic, tickDelta: Double) {

        view = camera.getView()
        projection = camera.getProjection()

        clearFrameBuffer()
        game.gameObjects.gameItems.forEachInstance<Renderable> { it.render(view, projection, tickDelta) }

        // This draws the buffer onto the screen
        FrameBuffer.unbind()
        clear()
        screenShader.use()
        screenShader.setUniforms(screenUniforms)
        buffer.draw()
        glClear(GL_DEPTH_BUFFER_BIT)
    }
}