package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.MovableDrawable
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.window.WindowI
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

interface StaticTextI: TextI, MovableDrawable {

    val window: WindowI
    override val size: Vec2 get() = getScreenSize()

    override fun getWidth(text: String): Float {
        val size = font.getWidth(text)
        return size * defaultCharHeight / window.aspectRatio
    }

    fun getScreenSize(): Vec2
    fun getScreenSize(text: String): Vec2 = Vec2(getWidth(text), defaultCharHeight)

    fun drawUnderline(model: Mat4, line: String = text, amount: Float = underlineAmount){
        val shader = MenuItem.translucentColourShader
        val newModel = model.scale(getWidth(line) * amount * window.aspectRatio / defaultCharHeight, underlineThickness, 0f).translate(0f, underlineOffset, 0f)

        shader.use()
        shader.setMat4("model", newModel)
        shader.setVec4("colour", colour)
        VertexShape.cornerSquareShape2D.bindAndDraw()
    }
}