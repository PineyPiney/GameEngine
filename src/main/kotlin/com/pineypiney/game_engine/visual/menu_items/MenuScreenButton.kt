package com.pineypiney.game_engine.visual.menu_items

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class MenuScreenButton(override val string: String, override var origin: Vec2, override val size: Vec2, override val textColour: Vec4, override val shader: Shader) : TextButton() {

    override val action: (button: Button) -> Unit = {}

    var change = false

    companion object{
        fun setChange(button: Button, state: Boolean){
            if(button is MenuScreenButton)
                button.change = state
        }
    }
}