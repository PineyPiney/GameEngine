package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.maths.I
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11C.GL_SRC_ALPHA

class Renderer(override val window: WindowI): BufferedGameRenderer<Game>() {

    var view = I
    var projection = I

    override fun init() {
        super.init()

        GLFunc.depthTest = false
        GLFunc.blend = true
        GLFunc.blendFunc = Vec2i(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        screenUniforms.setIntUniform("effects") { 0 }
    }

    override fun render(window: WindowI, game: Game, tickDelta: Double) {

        view = game.camera.getView()
        projection = game.camera.getProjection()

        clearFrameBuffer()
        game.gameObjects.gameItems.forEachInstance<Renderable> { it.render(view, projection, tickDelta) }

        // This draws the buffer onto the screen
        FrameBuffer.unbind()
        clear()
        screenShader.use()
        screenShader.setUniforms(screenUniforms)
        drawBufferTexture()
    }

    companion object{
        val screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
        val screenUniforms = screenShader.compileUniforms()
    }
}