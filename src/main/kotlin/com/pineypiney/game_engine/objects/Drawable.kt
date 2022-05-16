package com.pineypiney.game_engine.objects

import glm_.vec2.Vec2

interface Drawable: Visual {

    var origin: Vec2
    val size: Vec2

    fun draw()

    fun drawCentered(p: Vec2){
        origin = p - (size/2)
        draw()
    }

    fun drawCenteredLeft(p: Vec2) {
        origin = p - Vec2(0f, size.y * 0.5f)
        draw()
    }

    fun drawCenteredTop(p: Vec2) {
        origin = p - Vec2(size.x * 0.5f, size.y)
        draw()
    }

    fun drawCenteredRight(p: Vec2) {
        origin = p - Vec2(size.x, size.y * 0.5f)
        draw()
    }

    fun drawCenteredBottom(p: Vec2) {
        origin = p - Vec2(size.x * 0.5f, 0f)
        draw()
    }

    fun drawTopLeft(p: Vec2) {
        origin = p - Vec2(0, size.y)
        draw()
    }

    fun drawTopRight(p: Vec2) {
        origin = p - size
        draw()
    }

    fun drawBottomLeft(p: Vec2) {
        origin = p
        draw()
    }

    fun drawBottomRight(p: Vec2) {
        origin = p - Vec2(size.x, 0)
        draw()
    }
}