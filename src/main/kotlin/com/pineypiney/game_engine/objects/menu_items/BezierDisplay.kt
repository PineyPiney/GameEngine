package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class BezierDisplay(val p0: Vec2, val p1: Vec2, val p2: Vec2, val window: Window,
                    override val origin: Vec2 = Vec2(0),
                    override val size: Vec2 = Vec2(1)): MenuItem() {

    init {
        shader = bShader
    }

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec2Uniform("p0"){ p0 }
        uniforms.setVec2Uniform("p1"){ p1 }
        uniforms.setVec2Uniform("p2"){ p2 }
        uniforms.setVec2Uniform("windowSize"){ window.size / 2 }
        uniforms.setVec4Uniform("colour"){ Vec4(1) }
        uniforms.setFloatUniform("width"){ 0.0002f }
    }

    companion object{
        val bShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier2")]
    }
}