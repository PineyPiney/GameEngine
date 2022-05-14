package com.pineypiney.game_engine.renderers

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.visual.Deleteable
import com.pineypiney.game_engine.visual.ScreenObjectCollection
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL46C.*

abstract class GameRenderer: Deleteable {

    var vp: Mat4 = I

    fun init(){
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    abstract fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double)

    abstract fun updateAspectRatio(window: Window, objects: ScreenObjectCollection)

    private fun getPerspective(window: Window, camera: Camera): Mat4 = glm.perspective(camera.FOV.rad, window.aspectRatio, camera.range.x, camera.range.y)

    fun clear(){
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }
}