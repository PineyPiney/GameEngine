package com.pineypiney.game_engine.visual.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.visual.*
import com.pineypiney.game_engine.visual.util.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class MenuItem : IScreenObject, Drawable, Storable, Deleteable {

    override var visible: Boolean = true

    override val objects: MutableList<ScreenObjectCollection> = mutableListOf()

    override var origin: Vec2 = Vec2()
    override val size: Vec2 = Vec2()

    open val shape: Shape = Shape.cornerSquareShape
    open val shader: Shader = menuShader

    open fun setUniforms(){
        shader.setMat4("model", I.translate(Vec3(origin)).scale(Vec3(size)))
    }

    fun relative(x: Number, y: Number) = origin + (size * Vec2(x, y))
    fun relative(pos: Vec2) = origin + (size * pos)

    override fun addTo(objects: ScreenObjectCollection) {
        objects.guiItems.add(this)
    }

    override fun removeFrom(objects: ScreenObjectCollection) {
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
        objects.forEach { it.guiItems.remove(this)}
    }

    companion object{
        val menuShader: Shader = ShaderLoader.getShader(ResourceKey("vertex\\menu"), ResourceKey("fragment\\opaque_texture"))
        val menuShape: Shape = Shape.screenQuadShape

        fun between(point: Float, left: Float, size: Float) : Boolean{
            return left < point && point < left + size
        }

        fun between(point: Vec2, origin: Vec2, size: Vec2) : Boolean{
            return between(point.x, origin.x, size.x) &&
                    between(point.y, origin.y, size.y)
        }

        fun mouseBetween(origin: Vec2, size: Vec2) : Boolean{
            val point = Window.INSTANCE.input.mouse.lastPos
            return between(point.x, origin.x, size.x) &&
                    between(point.y, origin.y, size.y)
        }
    }
}