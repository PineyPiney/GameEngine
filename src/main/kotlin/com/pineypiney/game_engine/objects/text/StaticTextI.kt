package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.MovableDrawable
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.Shape
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

interface StaticTextI: TextI, MovableDrawable {

    val window: Window

    override fun getWidth(text: String): Float {
        val size = font.getSize(text)
        return size.x * defaultCharHeight / window.aspectRatio
    }

    fun getScreenSize(): Vec2
    fun getScreenSize(text: String): Vec2 = Vec2(getWidth(text), defaultCharHeight)

    fun drawUnderline(model: Mat4){
        val shader = MenuItem.translucentColourShader
        shader.use()
        shader.setMat4("model", model)
        shader.setVec4("colour", colour)
        Shape.cornerSquareShape2D.bind()
        Shape.cornerSquareShape2D.draw()
    }

    override fun drawCentered(p: Vec2){
        val size = getScreenSize()
        origin = p - (size/2)
        draw()
    }

    override fun drawCenteredLeft(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(0f, size.y * 0.5f)
        draw()
    }

    override fun drawCenteredTop(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x * 0.5f, size.y)
        draw()
    }

    override fun drawCenteredRight(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x, size.y * 0.5f)
        draw()
    }

    override fun drawCenteredBottom(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x * 0.5f, 0f)
        draw()
    }

    override fun drawTopLeft(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(0, size.y)
        draw()
    }

    override fun drawTopRight(p: Vec2) {
        val size = getScreenSize()
        origin = p - size
        draw()
    }

    override fun drawBottomLeft(p: Vec2) {
        origin = p
        draw()
    }

    override fun drawBottomRight(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x, 0)
        draw()
    }
}