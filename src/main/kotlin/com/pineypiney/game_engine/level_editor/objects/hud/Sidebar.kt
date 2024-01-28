package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL46C.*

abstract class Sidebar(var width: Float = 0.5f) : HudItem() {

    override var origin = Vec2(-1, -1)
    override var size = Vec2(width, 2)

    override var shader: Shader = translucentColourShader
    var colour = Vec4(0.5, 0.5, 0.5, 0.5)

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour"){ colour }
    }

    override fun draw() {
        // Enable Translucency
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        super.draw()

        glDisable(GL_BLEND)
    }
}