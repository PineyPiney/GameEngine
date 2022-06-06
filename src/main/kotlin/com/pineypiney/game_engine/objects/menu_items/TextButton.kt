package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.text.StretchyStaticText
import glm_.vec2.Vec2
import glm_.vec4.Vec4

open class TextButton(val string: String, override var origin: Vec2, final override val size: Vec2, window: Window, textColour: Vec4 = Vec4(0, 0, 0, 1), override val action: (button: AbstractButton) -> Unit) : AbstractButton() {

    private val text = StretchyStaticText(string, window, size, textColour)
    private var textPos = Vec2()

    var textColour: Vec4
        get() = text.colour
        set(value) { text.colour = value }

    override fun init(){
        super.init()

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