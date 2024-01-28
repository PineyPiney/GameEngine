package com.pineypiney.game_engine.level_editor.renderers

import com.pineypiney.game_engine.level_editor.LevelMakerScreen
import com.pineypiney.game_engine.rendering.FrameBuffer
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL46C.glViewport

open class SceneRenderer: PixelRenderer<LevelMakerScreen>() {

    val gameBuffer = FrameBuffer(Vec2i())

    override fun regenerateFrameBuffers(){
        val res = window.size
        buffer.setSize(res)
        gameBuffer.setSize(res)
    }

    override fun render(game: LevelMakerScreen, tickDelta: Double){

        view = camera.getView()
        projection = camera.getProjection()

        // First, clear the game texture
        clearFrameBuffer(gameBuffer)
        // And draw on the game
        renderItems(view, projection , tickDelta, game)

        if(game.unitGrid.visible) game.unitGrid.render(view, projection, 0.0)
        if(game.holding) game.selectedItem?.render(view, projection, 0.0)

        // Then clear the screen texture
        clearFrameBuffer(buffer)
        // Draw on the game screen
        drawTexture(gameBuffer)
        // And draw the HUD
        renderGUI(game)


        // Finally, clear the main screen
        glViewport(0, 0, window.width, window.height)
        FrameBuffer.unbind()
        clear()
        // And render the whole screen onto a screen quad
        drawTexture(buffer)
    }

    override fun deleteFrameBuffers(){
        super.deleteFrameBuffers()
        gameBuffer.delete()
    }
}