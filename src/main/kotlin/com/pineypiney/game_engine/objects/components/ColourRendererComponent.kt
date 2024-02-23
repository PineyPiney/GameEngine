package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class ColourRendererComponent(parent: GameObject, var colour: Vec4, shader: Shader = defaultShader, val vShape: VertexShape = VertexShape.centerSquareShape): RenderedComponent(parent, shader) {

    constructor(parent: GameObject, colour: Vec3, shader: Shader = defaultShader, vShape: VertexShape = VertexShape.centerSquareShape): this(parent, Vec4(colour, 1f), shader, vShape)

    constructor(parent: GameObject): this(parent, Vec4(1f, 1f, 1f, 1f))

    override val renderSize: Vec2 = Vec2(1f, 1f)

    override val shape: Shape get() = vShape.shape

    override val fields: Array<Field<*>> = arrayOf(
        Vec4Field("clr", ::colour){ colour = it }
    )

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour", ::colour)
    }

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        shader.setUp(uniforms, renderer)
        vShape.bindAndDraw()
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/colour"))
        val menuShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour"))
    }
}