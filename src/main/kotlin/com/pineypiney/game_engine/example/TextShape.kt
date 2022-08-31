package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class TextShape(val points: Array<Vec2>, val beziers: IntArray, val window: Window, val textSize: Vec2 = Vec2(1)): MenuItem() {

    override fun init() {
        shader = textShader
        super.init()
    }

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec2sUniform("points"){ points.map { it * textSize } }
        uniforms.setUIntsUniform("bezierEnds"){ beziers }
        uniforms.setVec2Uniform("windowSize"){ window.size / 2 }
        uniforms.setVec4Uniform("colour"){ Vec4(1) }
        uniforms.setFloatUniform("width"){ 0.00006f }
    }

    companion object{
        val textShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/ttf")]
    }
}