package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.max

open class SpriteComponent(parent: GameObject, texture: Texture = Texture.broke, pixelsPerUnit: Float = 100f, shader: Shader = defaultShader, val vShape: VertexShape = VertexShape.centerSquareShape): RenderedComponent(parent, shader) {

    var texture: Texture = texture
        set(value) {
            field = value
            renderSize = Vec2(texture.width * pixelSize, texture.height * pixelSize)
        }

    var pixelsPerUnit: Float = pixelsPerUnit
        set(value) {
            field = max(value, 0.000001f)
            pixelSize = 1f / pixelSize
            renderSize = Vec2(texture.width * pixelSize, texture.height * pixelSize)
        }
    var pixelSize: Float = 1f / pixelsPerUnit; private set

    override var renderSize: Vec2 = Vec2(texture.width * pixelSize, texture.height * pixelSize)

    override val shape: Shape get() = vShape.shape

    constructor(parent: GameObject): this(parent, Texture.broke)

    override val fields: Array<Field<*>> = arrayOf(
        Field("txr", ::DefaultFieldEditor, ::texture, { this.texture = it }, { it.fileLocation.substringBefore('.') }, { _, s -> TextureLoader[ResourceKey(s)]} ),
        FloatField("ppu", ::pixelsPerUnit) { this.pixelsPerUnit = it }
    )

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setMat4Uniform("model"){ parent.worldModel * I.scale(Vec3(renderSize, 1f)) }
    }

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        shader.setUp(uniforms, renderer)
        texture.bind()
        vShape.bindAndDraw()
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/texture"))
    }
}