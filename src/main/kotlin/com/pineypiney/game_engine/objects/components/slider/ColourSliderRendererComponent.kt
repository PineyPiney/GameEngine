package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec4.Vec4

open class ColourSliderRendererComponent(parent: GameObject, shader: Shader, val colours: MutableMap<String, Float>) : RenderedComponent(parent, shader) {

    val vShape = VertexShape.cornerSquareShape
    override val renderSize: Vec2 = Vec2()
    override val shape: Shape = vShape.shape

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setFloatUniform("outlineThickness"){ 0.005f }
        uniforms.setVec4Uniform("outlineColour"){ Vec4(0, 0, 0, 1) }
        for((colour, _) in colours){
            uniforms.setFloatUniform(colour){ colours[colour] ?: 0f }
        }
    }

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        shader.setUp(uniforms, renderer)
        vShape.bindAndDraw()
    }

    operator fun get(colour: String) = colours[colour] ?: 0f
    operator fun set(colour: String, value: Float){ colours[colour] = value }

    companion object{
        val redShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/red_slider")))
        val greenShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/green_slider")))
        val blueShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/blue_slider")))
    }
}