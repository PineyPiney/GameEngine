package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Text
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec4.Vec4

abstract class TextButton : Button() {

    abstract val string: String
    abstract val textColour: Vec4

    private var text = Text("")
    private var textPos = Vec2()

    override fun init(){
        super.init()

        baseTexture = TextureLoader.getTexture(ResourceKey("menu_items\\buttons\\button"))
        hoverTexture = TextureLoader.getTexture(ResourceKey("menu_items\\buttons\\buttonHover"))
        clickTexture = TextureLoader.getTexture(ResourceKey("menu_items\\buttons\\buttonClick"))

        text = Text(string, size, textColour)
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