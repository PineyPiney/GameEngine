package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.objects.components.PreRenderComponent
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11C.*

class Renderer<R: CameraI>(override val window: WindowI, override val camera: R): BufferedGameRenderer<WindowGameLogic>() {

    override fun init() {
        super.init()

        GLFunc.blend = true
        GLFunc.blendFunc = Vec2i(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        screenUniforms.setIntUniform("effects") { 0 }
    }

    override fun render(game: WindowGameLogic, tickDelta: Double) {

        view = camera.getView()
        projection = camera.getProjection()

        clearFrameBuffer()
        GLFunc.depthTest = true
        for(o in game.gameObjects.gameItems.flatMap { it.allDescendants() }) {
            val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
            if(renderedComponents.isNotEmpty()){
                for(c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(tickDelta)
                for(c in renderedComponents) c.render(this, tickDelta)
            }
        }

        GLFunc.depthTest = false
        for(o in game.gameObjects.guiItems.flatMap { it.allDescendants() }) {
            val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
            if(renderedComponents.isNotEmpty()){
                for(c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(tickDelta)
                for(c in renderedComponents) c.render(this, tickDelta)
            }
        }

        // This draws the buffer onto the screen
        FrameBuffer.unbind()
        clear()
        screenShader.use()
        screenShader.setUniforms(screenUniforms, this)
        buffer.draw()
        glClear(GL_DEPTH_BUFFER_BIT)
    }
}