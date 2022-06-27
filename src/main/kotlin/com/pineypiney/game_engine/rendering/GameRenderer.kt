package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import org.lwjgl.opengl.GL46C.*

abstract class GameRenderer<E: IGameLogic>: Initialisable {

    abstract val window: Window

    abstract fun render(window: Window, game: E, tickDelta: Double)

    abstract fun updateAspectRatio(window: Window, objects: ObjectCollection)

    fun clear(){
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }
}