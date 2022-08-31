package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec2.Vec2t
import glm_.vec4.Vec4

class BezierDisplay(val points: Array<Vec2t<*>>, val window: Window,
                    override val origin: Vec2 = Vec2(0),
                    override val size: Vec2 = Vec2(1)): MenuItem() {

    init {
        shader = when(points.size){
            4 -> b3Shader
            3 -> b2Shader
            else -> b1Shader
        }
    }

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec2sUniform("points"){ points.asList() }
        uniforms.setVec2Uniform("windowSize"){ window.size / 2 }
        uniforms.setVec4Uniform("colour"){ Vec4(1) }
        uniforms.setFloatUniform("width"){ 0.0002f }
    }

    companion object{
        val b1Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier1")]
        val b2Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier2")]
        val b3Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier3")]
    }
}