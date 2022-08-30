package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2i
import java.awt.Font as JavaFont

class TrueTypeFont(val font: JavaFont): Font() {

    override val shader: Shader
        get() = TODO("Not yet implemented")

    override fun getCharWidth(char: Char): Int {
        TODO("Not yet implemented")
    }

    override fun getCharHeight(char: Char): Vec2i {
        TODO("Not yet implemented")
    }

    override fun getPixelWidth(text: String): Int {
        TODO("Not yet implemented")
    }
}