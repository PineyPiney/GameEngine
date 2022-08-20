package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4

abstract class OutlinedSlider(): Slider() {

    override var shader: Shader = sliderShader

    open var colour: Vec4 = Vec4(0.7)
    open var outlineThickness: Float = 0.005f
    open var outlineColour: Vec4 = Vec4(0.5, 0.5, 0.5, 1)

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setFloatUniform("outlineThickness"){ outlineThickness }
        uniforms.setVec4Uniform("outlineColour"){ outlineColour }
        uniforms.setVec4Uniform("colour"){ colour }
    }

    companion object {
        val sliderShader = ShaderLoader.getShader(ResourceKey("vertex/2D_pass_pos"), ResourceKey("fragment/sliders/outlined_slider"))
    }
}