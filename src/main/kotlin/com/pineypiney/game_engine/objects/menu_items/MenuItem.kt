package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.*
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

abstract class MenuItem : Initialisable, Storable, Drawable, Shaded {

    override var visible: Boolean = true

    override val objects: MutableSet<ObjectCollection> = mutableSetOf()

    override var origin: Vec2 = Vec2()
    override val size: Vec2 = Vec2()

    open val shape: Shape = menuShape
    override var shader: Shader = opaqueColourShader
        set(value) {
            field = value
            uniforms = field.compileUniforms()
        }
    override var uniforms = Uniforms.default
        set(value) {
            field = value
            setUniforms()
        }

    override fun init() {
        uniforms = shader.compileUniforms()
    }

    override fun setUniforms(){
        uniforms.setMat4Uniform("model") { model }
    }

    override fun draw() {
        shader.use()
        shader.setUniforms(uniforms)

        shape.bind()
        shape.draw()
    }

    fun relative(x: Number, y: Number) = origin + (size * Vec2(x, y))
    fun relative(pos: Vec2) = origin + (size * pos)

    override fun addTo(objects: ObjectCollection) {
        objects.guiItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.guiItems.remove(this)
    }

    override fun drawCentered(p: Vec2){
        origin = p - (size/2)
        draw()
    }

    override fun drawCenteredLeft(p: Vec2) {
        origin = p - Vec2(0f, size.y * 0.5f)
        draw()
    }

    override fun drawCenteredTop(p: Vec2) {
        origin = p - Vec2(size.x * 0.5f, size.y)
        draw()
    }

    override fun drawCenteredRight(p: Vec2) {
        origin = p - Vec2(size.x, size.y * 0.5f)
        draw()
    }

    override fun drawCenteredBottom(p: Vec2) {
        origin = p - Vec2(size.x * 0.5f, 0f)
        draw()
    }

    override fun drawTopLeft(p: Vec2) {
        origin = p - Vec2(0, size.y)
        draw()
    }

    override fun drawTopRight(p: Vec2) {
        origin = p - size
        draw()
    }

    override fun drawBottomLeft(p: Vec2) {
        origin = p
        draw()
    }

    override fun drawBottomRight(p: Vec2) {
        origin = p - Vec2(size.x, 0)
        draw()
    }

    override fun delete() {
        for(it in objects) { it.guiItems.remove(this)}
    }

    companion object{
        val opaqueTextureShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/texture_opaque"))
        val transparentTextureShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/texture"))
        val opaqueColourShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour_opaque"))
        val translucentColourShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour"))
        val menuShape: Shape = Shape.cornerSquareShape2D
    }
}