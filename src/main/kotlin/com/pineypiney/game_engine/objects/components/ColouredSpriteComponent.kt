package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec4.Vec4

open class ColouredSpriteComponent(parent: GameObject, texture: Texture, pixelsPerUnit: Float = 100f, var tint: () -> Vec4 = { Vec4(1f, 1f, 1f, 1f) }, shader: Shader = defaultShader, shape: VertexShape = VertexShape.centerSquareShape, val setUniforms: ColouredSpriteComponent.() -> Unit = {}): SpriteComponent(parent, texture, pixelsPerUnit, shader, shape) {

    constructor(parent: GameObject, texture: Texture, pixelsPerUnit: Float = 100f, tint: Vec4 = Vec4(1f, 1f, 1f, 1f), shader: Shader = defaultShader, shape: VertexShape = VertexShape.centerSquareShape, setUniforms: ColouredSpriteComponent.() -> Unit = {}): this(parent, texture, pixelsPerUnit, { tint }, shader, shape, setUniforms)

    constructor(parent: GameObject): this(parent, Texture.broke, 100f, { Vec4(1f) })

    override val fields: Array<Field<*>> = super.fields + arrayOf(
        Vec4Field("clr", tint) { tint =  { it } }
    )

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour"){ this.tint() }
        setUniforms.invoke(this)
    }
}