package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.renderers.BufferedGameRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey

class Renderer(override val window: Window): BufferedGameRenderer() {

    var view = I
    var projection = I

    override fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double) {

        view = camera.getViewMatrix()
        projection = getPerspective(window, camera)

        clearFrameBuffer()
        game.gameObjects.forEachRendered { it.render(view, projection, tickDelta) }

        // This draws the buffer onto the screen
        clearFrameBuffer(0)
        screenShader.use()
        screenShader.setInt("effects", 0)
        drawBufferTexture()
    }

    companion object{
        val screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
    }
}