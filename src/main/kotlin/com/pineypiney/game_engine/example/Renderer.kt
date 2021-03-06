package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.maths.I
import org.lwjgl.opengl.GL46C.*

class Renderer(override val window: Window): BufferedGameRenderer<Game>() {

    var projection = I

    override fun init() {
        super.init()

        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        screenUniforms.setIntUniform("effects") { 0 }
    }

    override fun render(window: Window, game: Game, tickDelta: Double) {

        projection = game.camera.getPerspective()

        clearFrameBuffer()
        glEnable(GL_DEPTH_TEST)
        game.gameObjects.gameItems.forEachInstance<Renderable> { it.render(I, projection, tickDelta) }

        // This draws the buffer onto the screen
        clearFrameBuffer(0)
        screenShader.use()
        screenShader.setUniforms(screenUniforms)
        drawBufferTexture()
    }

    companion object{
        val screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
        val screenUniforms = screenShader.compileUniforms()
    }
}