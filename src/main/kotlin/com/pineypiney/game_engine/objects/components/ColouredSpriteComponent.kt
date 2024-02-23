package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec4.Vec4

open class ColouredSpriteComponent(parent: GameObject, texture: Texture, pixelsPerUnit: Float = 100f, var colour: Vec4 = Vec4(1f, 1f, 1f, 1f), shader: Shader = defaultShader, shape: VertexShape = VertexShape.centerSquareShape): SpriteComponent(parent, texture, pixelsPerUnit, shader, shape) {

    constructor(parent: GameObject): this(parent, Texture.broke)

    override val fields: Array<Field<*>> = super.fields + arrayOf(
        Vec4Field("clr", ::colour) { colour = it }
    )

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour", ::colour)
    }
}