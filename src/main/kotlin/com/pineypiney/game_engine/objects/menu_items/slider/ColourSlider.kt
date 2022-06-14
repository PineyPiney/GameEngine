package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import glm_.i
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ColourSlider(size: Vec2, window: Window, override var shader: Shader, val action: (Int) -> Unit) : Slider(size, 0f, 255f, 255f, window) {

    override val pointer: ColourSliderPointer = ColourSliderPointer(this, 1f)

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setFloatUniform("outlineThickness"){ 0.005f }
        uniforms.setVec4Uniform("outlineColour"){ Vec4(0, 0, 0, 1) }
        uniforms.setMat4Uniform("vp"){ I }
    }

    override fun moveSliderTo(move: Float) {
        super.moveSliderTo(move)
        action(value.i)
    }

    companion object{
        val redShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey(("fragment/sliders/red_slider")))
        val greenShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey(("fragment/sliders/green_slider")))
        val blueShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey(("fragment/sliders/blue_slider")))
    }
}