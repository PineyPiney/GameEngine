package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import org.lwjgl.opengl.GL11C.*

abstract class GameRenderer<E: GameLogicI>: Initialisable {

    abstract val window: WindowI

    abstract fun render(window: WindowI, game: E, tickDelta: Double)

    abstract fun updateAspectRatio(window: WindowI, objects: ObjectCollection)

    open fun clear(){
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }
}