package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import org.lwjgl.opengl.GL11.*

abstract class GameRenderer<E: IGameLogic>: Initialisable {

    abstract val window: Window

    abstract fun render(window: Window, game: E, tickDelta: Double)

    abstract fun updateAspectRatio(window: Window, objects: ObjectCollection)

    open fun clear(){
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }
}