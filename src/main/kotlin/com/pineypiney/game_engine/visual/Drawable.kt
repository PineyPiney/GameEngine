package com.pineypiney.game_engine.visual

import glm_.vec2.Vec2

interface Drawable: Visual {

    var origin: Vec2
    val size: Vec2

    fun draw()

    fun drawCentered(p: Vec2) = draw()

    fun drawCenteredLeft(p: Vec2) = draw()

    fun drawCenteredTop(p: Vec2) = draw()

    fun drawCenteredRight(p: Vec2) = draw()

    fun drawCenteredBottom(p: Vec2) = draw()

    fun drawTopLeft(p: Vec2) = draw()

    fun drawTopRight(p: Vec2) = draw()

    fun drawBottomLeft(p: Vec2) = draw()

    fun drawBottomRight(p: Vec2) = draw()
}