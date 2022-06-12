package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class MenuScreenButton(string: String, origin: Vec2, size: Vec2, window: Window, textColour: Vec4, override var shader: Shader) : TextButton(string, origin, size, window, textColour, setChange) {

    override val action: (button: AbstractButton) -> Unit = {}

    var change = false

    companion object{
        val setChange: (AbstractButton) -> Unit = { button ->
            if(button is MenuScreenButton) button.change = button.pressed
        }
    }
}