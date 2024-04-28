package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2

interface RenderedComponentI: ComponentI {

    var visible: Boolean
    val renderSize: Vec2
    val shape: Shape

    val shader: Shader
    val uniforms: Uniforms

    fun setUniforms()
    fun render(renderer: RendererI<*>, tickDelta: Double)
}