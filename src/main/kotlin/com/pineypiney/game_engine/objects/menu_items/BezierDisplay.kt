package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec2.Vec2t
import glm_.vec4.Vec4

class BezierDisplay(parent: GameObject, val points: Array<Vec2t<*>>): RenderedComponent(parent, when(points.size){
    4 -> b3Shader
    3 -> b2Shader
    else -> b1Shader
}) {

    val vShape = VertexShape.cornerSquareShape
    override val renderSize: Vec2 = Vec2(1f)
    override val shape: Shape = vShape.shape

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec2sUniform("points"){ points.asList() }
        uniforms.setVec4Uniform("colour"){ Vec4(1) }
        uniforms.setFloatUniform("width"){ 0.0002f }
    }

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        shader.setUp(uniforms, renderer)
        vShape.bindAndDraw()
    }

    companion object{
        val b1Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier1")]
        val b2Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier2")]
        val b3Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier3")]
    }
}