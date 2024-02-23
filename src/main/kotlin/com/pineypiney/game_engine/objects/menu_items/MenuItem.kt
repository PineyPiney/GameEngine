package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class MenuItem : GameObject() {

    override var name: String = "MenuItem"

    override fun addTo(objects: ObjectCollection) {
        objects.guiItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.guiItems.remove(this)
    }

    fun os(origin: Vec2, size: Vec2){
        position = Vec3(origin, 0f)
        scale = Vec3(size, 1f)
    }

    override fun delete() {
        objects?.guiItems?.remove(this)
        objects = null
    }

    companion object{
        val opaqueTextureShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/texture_opaque"))
        val transparentTextureShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/texture"))
        val opaqueColourShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour_opaque"))
        val translucentColourShader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour"))
        val menuShape: VertexShape = VertexShape.cornerSquareShape
    }
}