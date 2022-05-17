package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class BasicSlider(override var origin: Vec2, size: Vec2, low: Float, high: Float, var colour: Vec4 = Vec4(0.7), var outlineThickness: Float = 0.005f, var outlineColour: Vec4 = Vec4(0.5, 0.5, 0.5, 1)) : Slider(size, low, high, low) {

    override val pointer: SliderPointer = BasicSliderPointer(this, size * Vec2((size.y / size.x) * (SliderPointer.pointerTexture.aspectRatio / Window.INSTANCE.aspectRatio) , 1))
    override val shader: Shader = sliderShader

    override fun setUniforms() {
        super.setUniforms()
        shader.setFloat("aspect", Window.INSTANCE.aspectRatio)
        shader.setVec4("colour", colour)
        shader.setFloat("outlineThickness", outlineThickness)
        shader.setVec4("outlineColour", outlineColour)
    }

    companion object {
        val sliderShader = ShaderLoader.getShader(ResourceKey("vertex/2D_pass_pos"), ResourceKey("fragment/sliders/outlined_slider"))
    }
}