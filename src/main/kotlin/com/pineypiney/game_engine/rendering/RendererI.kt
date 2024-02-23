package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.Initialisable
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11C

interface RendererI<E: GameLogicI>: Initialisable {

    val viewPos: Vec3
    var view: Mat4
    var projection: Mat4
    var viewportSize: Vec2i
    val aspectRatio: Float

    val numPointLights: Int

    fun render(game: E, tickDelta: Double)

    fun clear(){
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT or GL11C.GL_DEPTH_BUFFER_BIT or GL11C.GL_STENCIL_BUFFER_BIT)
    }
}