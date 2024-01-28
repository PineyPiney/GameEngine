package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.*
import com.pineypiney.game_engine.objects.util.components.Component
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

abstract class MenuItem : Initialisable, Storable, Drawable, Shaded {

    override var name: String = "MenuItem"

    override var visible: Boolean = true

    override var objects: ObjectCollection? = null
    override var components: MutableSet<Component> = mutableSetOf()
    override val children: MutableSet<Storable> = mutableSetOf()

    override val origin: Vec2 = Vec2()
    override val size: Vec2 = Vec2(1)

    open val shape: VertexShape = menuShape
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
        shader.setUp(uniforms)
        shape.bindAndDraw()
    }

    fun relative(x: Number, y: Number) = origin + (size * Vec2(x, y))
    fun relative(pos: Vec2) = origin + (size * pos)


    fun addChild(vararg child: Storable){
        this.children.addAll(child)
    }
    fun addChildren(children: Iterable<Storable>){
        this.children.addAll(children)
    }
    fun removeChild(vararg child: Storable){
        this.children.removeAll(child.toSet())
    }
    fun removeChildren(children: Iterable<Storable>){
        this.children.removeAll(children.toSet())
    }

    override fun addTo(objects: ObjectCollection) {
        objects.guiItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.guiItems.remove(this)
    }

    override fun delete() {
        objects?.guiItems?.remove(this)
    }

    companion object{
        val opaqueTextureShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/texture_opaque"))
        val transparentTextureShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/texture"))
        val opaqueColourShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour_opaque"))
        val translucentColourShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour"))
        val menuShape: VertexShape = VertexShape.cornerSquareShape2D
    }
}