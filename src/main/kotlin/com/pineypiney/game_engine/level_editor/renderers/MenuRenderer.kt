package com.pineypiney.game_engine.level_editor.renderers

import com.pineypiney.game_engine.level_editor.screens.MenuScreen
import com.pineypiney.game_engine.rendering.FrameBuffer
import org.lwjgl.opengl.GL46C.glViewport

open class MenuRenderer: PixelRenderer<MenuScreen>() {

    override fun regenerateFrameBuffers(){
        val res = window.size
        buffer.setSize(res)
    }

    override fun render(game: MenuScreen, tickDelta: Double){

        view = game.camera.getView()
        projection = game.camera.getProjection()

        // First clear the screen texture
        clearFrameBuffer(buffer)
        // And draw the HUD
        renderGUI(game)

        // Then, clear the main screen
        glViewport(0, 0, window.width, window.height)
        FrameBuffer.unbind()
        clear()
        // And render the whole screen onto a screen quad
        drawTexture(buffer)
    }
}