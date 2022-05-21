package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.text.StretchyStaticText
import glm_.vec2.Vec2
import glm_.vec4.Vec4

abstract class TextButton : Button() {

    abstract val string: String
    abstract val textColour: Vec4

    private var text = StretchyStaticText("")
    private var textPos = Vec2()

    override fun init(){
        super.init()

        text = StretchyStaticText(string, size, textColour)
        text.init()
        textPos = origin + (size * 0.5)
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
    }

    override fun draw() {
        super.draw()
        text.drawCentered(textPos)
    }

    override fun delete() {
        super.delete()
        text.delete()
    }
}