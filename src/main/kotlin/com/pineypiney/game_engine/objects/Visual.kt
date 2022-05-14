package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.Window
import org.lwjgl.opengl.GL46C

interface Visual {

    var visible: Boolean

    fun updateAspectRatio(window: Window) {}

    fun drawArrays(vertices: Int = 6){
        GL46C.glDrawArrays(GL46C.GL_TRIANGLES, 0, vertices)
    }
}