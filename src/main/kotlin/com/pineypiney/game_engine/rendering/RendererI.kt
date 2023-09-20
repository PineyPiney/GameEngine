package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.Initialisable
import org.lwjgl.opengl.GL11C

interface RendererI<E: GameLogicI>: Initialisable {

    fun render(game: E, tickDelta: Double)

    fun clear(){
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT or GL11C.GL_DEPTH_BUFFER_BIT or GL11C.GL_STENCIL_BUFFER_BIT)
    }
}