package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.cameras.Camera
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL46C.*

abstract class GameRenderer: Deleteable {

    abstract val window: Window

    open fun init(){
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    abstract fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double)

    abstract fun updateAspectRatio(window: Window, objects: ObjectCollection)

    open fun getPerspective(window: Window, camera: Camera): Mat4 = glm.perspective(camera.FOV.rad, window.aspectRatio, camera.range.x, camera.range.y)

    fun clear(){
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }
}