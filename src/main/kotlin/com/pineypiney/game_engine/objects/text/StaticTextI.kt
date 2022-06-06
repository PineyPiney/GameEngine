package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Drawable
import glm_.f
import glm_.vec2.Vec2

interface StaticTextI: TextI, Drawable {

    val window: Window

    fun getScreenSize(): Vec2

    fun getScreenSize(text: String): Vec2 {
        return Vec2(defaultCharWidth * (getPixelWidth(text).f/ font.letterWidth), defaultCharHeight)
    }

    override fun pixelToRelative(pixel: Int): Float {
        return pixel * (defaultCharHeight / font.letterHeight) / window.aspectRatio
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