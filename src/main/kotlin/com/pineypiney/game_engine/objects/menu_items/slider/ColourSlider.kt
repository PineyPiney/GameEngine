package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import glm_.i
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ColourSlider(size: Vec2, window: Window, override val shader: Shader, val uniforms: (Shader) -> Unit, val action: (Int) -> Unit) : Slider(size, 0f, 255f, 255f, window) {

    override val pointer: ColourSliderPointer = ColourSliderPointer(this, size * Vec2((size.y / size.x) * (SliderPointer.pointerTexture.aspectRatio / window.aspectRatio) , 1))

    override fun setUniforms() {
        super.setUniforms()
        shader.setMat4("vp", I)
        shader.setFloat("aspect", window.aspectRatio)
        shader.setFloat("outlineThickness", 0.005f)
        shader.setVec4("outlineColour", Vec4(0, 0, 0, 1))
        uniforms(shader)
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