package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ColourSlider(override val origin: Vec2, override val size: Vec2, override val window: WindowI, override var shader: Shader, val colours: MutableMap<String, Float>) : Slider() {

    override val low: Float = 0f
    override val high: Float = 255f
    override var value: Float = 255f

    override val pointer: SliderPointer = BasicSliderPointer(this, 1f)

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setFloatUniform("outlineThickness"){ 0.005f }
        uniforms.setVec4Uniform("outlineColour"){ Vec4(0, 0, 0, 1) }
        for((colour, _) in colours){
            uniforms.setFloatUniform(colour){ colours[colour] ?: 0f }
        }
    }

    operator fun get(colour: String) = colours[colour] ?: 0f
    operator fun set(colour: String, value: Float){ colours[colour] = value }

    companion object{
        val redShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/red_slider")))
        val greenShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/green_slider")))
        val blueShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/blue_slider")))
    }
}