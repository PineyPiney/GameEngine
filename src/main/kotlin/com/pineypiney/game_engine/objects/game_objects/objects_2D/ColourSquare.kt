package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.maths.normal
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ColourSquare(var colour: Vec4 = Vec4(1), override val size: Vec2 = Vec2(0.1f)): MenuItem() {

    override var shader: Shader = translucentColourShader
    override val shape: VertexShape = VertexShape.centerSquareShape2D

    override var origin: Vec2 = super.origin
    var rotation = 0f

    override val model: Mat4
        get() = super.model.rotate(rotation, normal)

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour"){ colour }
    }
}