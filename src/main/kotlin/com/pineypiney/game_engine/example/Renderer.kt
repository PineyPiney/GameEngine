package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.maths.I
import org.lwjgl.opengl.GL11C.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11C.glEnable

class Renderer(override val window: Window): BufferedGameRenderer() {

    var view = I
    var projection = I

    override fun init() {
        super.init()

        screenUniforms.setIntUniform("effects") { 0 }
    }

    override fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double) {

        view = camera.getViewMatrix()
        projection = getPerspective(window, camera)

        clearFrameBuffer()
        glEnable(GL_DEPTH_TEST)
        game.gameObjects.gameItems.forEachInstance<Renderable> { it.render(view, projection, tickDelta) }

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