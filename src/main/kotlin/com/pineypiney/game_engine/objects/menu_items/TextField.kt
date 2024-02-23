package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.TextFieldComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import glm_.vec2.Vec2
import glm_.vec4.Vec4

open class TextField(origin: Vec2, size: Vec2, val textOffset: Float = 0f, val textSize: Float = 1f): MenuItem() {

    init {
        os(origin, size)
    }

    override fun addComponents() {
        super.addComponents()
        components.add(TextFieldComponent(this, textOffset, textSize))
        components.add(ColourRendererComponent(this, Vec4(0.5f, .5f, .5f, 1f), translucentColourShader, VertexShape.cornerSquareShape))
    }
}